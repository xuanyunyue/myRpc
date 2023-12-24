package github.xuanyunyue.remoting.transport.netty.client;

import enums.CompressTypeEnum;
import enums.SerializationTypeEnum;
import factory.SingletonFactory;
import github.xuanyunyue.remoting.constants.RPCConstants;
import github.xuanyunyue.remoting.dto.RPCMessage;
import github.xuanyunyue.remoting.dto.RPCResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author： zyx1128
 * @create： 2023/12/20 18:14
 * @description：TODO
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler {

    // 用于存放未被服务端处理的请求（建议限制 map 容器大小，避免未处理请求过多 OOM)。
    private final UnprocessedRequests unprocessedRequests;
    private final NettyClient nettyClient;

    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object transportMsg) throws Exception {
        log.info("client receive msg: [{}]", transportMsg);
        if (transportMsg instanceof RPCMessage) {
            RPCMessage msg = (RPCMessage) transportMsg;
            byte messageType = msg.getMessageType();
            if (messageType == RPCConstants.HEARTBEAT_RESPONSE_TYPE) {
                log.info("heart [{}]", msg.getData());
            }
            if (messageType == RPCConstants.RESPONSE_TYPE) {
                // 获取rpcMessage里面的rpcResponse
                RPCResponse rpcResponse = (RPCResponse) msg.getData();
                // 从unprocessedRequests移除发送时添加的请求
                unprocessedRequests.complete(rpcResponse);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 处理用户自定义的事件。当 Channel 中的 ChannelPipeline 中的某个处理器使用 fireUserEventTriggered() 方法触发用户自定义事件时，这个方法会被调用。
     * 这个方法常常被用来处理特定的自定义事件，比如空闲状态检测、心跳检测、重新连接策略等。
     * 这里就是用来封装心跳包的
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                // 根据客户端的地址获取对应的单例channel ============这里获取单例的channel和直接获取channel有什么区别===================
                Channel channel = nettyClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RPCMessage rpcMessage = new RPCMessage();
                // ===============为什么示例中的心跳包的序列化是用PROTOSTUFF呢================
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RPCConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RPCConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
