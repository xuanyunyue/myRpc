package github.xuanyunyue.remoting.transport.netty.server;

import github.xuanyunyue.enums.CompressTypeEnum;
import github.xuanyunyue.enums.RPCResponseCodeEnum;
import github.xuanyunyue.enums.SerializationTypeEnum;
import github.xuanyunyue.factory.SingletonFactory;
import github.xuanyunyue.remoting.constants.RPCConstants;
import github.xuanyunyue.remoting.dto.RPCMessage;
import github.xuanyunyue.remoting.dto.RPCRequest;
import github.xuanyunyue.remoting.dto.RPCResponse;
import github.xuanyunyue.remoting.handler.RPCRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import static com.sun.activation.registries.LogSupport.log;

/**
 * @author： zyx1128
 * @create： 2023/12/7 19:19
 * @description：TODO
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler {

    //处理方法调用请求的handler
    private final RPCRequestHandler rpcRequestHandler;
    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RPCRequestHandler.class);
    }

    /**
     * - @description: 此方法用于封装服务端向客户端发送的RPCMessage。若是客户端的调用请求，则通过RPCRequestHandler类下的handle方法调用方法并返回
     *
     * @param ctx
     * @param msg - @return void
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        //规定好了消息是用RPCMessage封装的
        if (msg instanceof RPCMessage) {
            String clientAddress = ctx.channel().remoteAddress().toString();
            log.info("server has receive msg:[{}]    from client [{}]", msg, clientAddress);
            //获取消息的type：调用请求/心跳包
            byte messageType = ((RPCMessage) msg).getMessageType();
            //返回给客户端的传输过程的RPCMessage
            RPCMessage rpcMessage = new RPCMessage();
            rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());//=================为什么实例用的是hession，这到底是序列化rpcmessage还是其他的==============
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            if (messageType== RPCConstants.REQUEST_TYPE){//处理客户端的调用请求
                //获取RPCRequest
                RPCRequest request = (RPCRequest) ((RPCMessage) msg).getData();
                //调用对应方法，返回result
                Object result = rpcRequestHandler.handle(request);
                log.info(String.format("server get result: %s", result.toString()));
                //设置返回的MessageType
                rpcMessage.setMessageType(RPCConstants.RESPONSE_TYPE);

                //判断通道是否正常
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    RPCResponse<Object> rpcResponse = RPCResponse.success(result, request.getRequestId());
                    rpcMessage.setData(rpcResponse);
                } else {
                    RPCResponse<Object> rpcResponse = RPCResponse.fail(RPCResponseCodeEnum.FAIL);
                    rpcMessage.setData(rpcResponse);
                    log.error("not writable now, message dropped. location:github.xuanyunyue.remoting.netty.server.NettyServerHandler's method: channelRead0");
                }
            }else {//处理心跳包，心跳包不需要requestId
                rpcMessage.setMessageType(RPCConstants.HEARTBEAT_RESPONSE_TYPE);
                rpcMessage.setData(RPCConstants.PONG);
            }
            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }

    }
}
