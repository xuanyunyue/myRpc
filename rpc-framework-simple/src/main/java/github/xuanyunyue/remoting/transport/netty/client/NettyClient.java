package github.xuanyunyue.remoting.transport.netty.client;

import github.xuanyunyue.enums.CompressTypeEnum;
import github.xuanyunyue.enums.SerializationTypeEnum;
import github.xuanyunyue.enums.ServiceDiscoveryEnum;
import github.xuanyunyue.extension.ExtensionLoader;
import github.xuanyunyue.factory.SingletonFactory;
import github.xuanyunyue.registry.ServiceDiscovery;
import github.xuanyunyue.remoting.constants.RPCConstants;
import github.xuanyunyue.remoting.dto.RPCMessage;
import github.xuanyunyue.remoting.dto.RPCRequest;
import github.xuanyunyue.remoting.dto.RPCResponse;
import github.xuanyunyue.remoting.transport.SendRequest;
import github.xuanyunyue.remoting.transport.netty.codec.RPCMessageDecoder;
import github.xuanyunyue.remoting.transport.netty.codec.RPCMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author： zyx1128
 * @create： 2023/12/20 17:49
 * @description：TODO
 */
@Slf4j
public class NettyClient implements SendRequest {
    private final ServiceDiscovery serviceDiscovery;
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;


    public NettyClient() {
        // initialize resources such as EventLoopGroup, Bootstrap
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //  The timeout period of the connection.
                //  If this time is exceeded or the connection cannot be established, the connection fails.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // If no data is sent to the server within 15 seconds, a heartbeat request is sent
                        // 参数为0表示禁用，这里应该是表示若5s内没有写操作，则发送心跳包才对
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RPCMessageEncoder());
                        p.addLast(new RPCMessageDecoder());
                        p.addLast(new NettyClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }


    /**
     - @description: TODO 根据传进来的InetSocketAddress得到channel
     * @param serverAddress
    - @return io.netty.channel.Channel
     */
    public Channel getChannel(InetSocketAddress serverAddress) {
        Channel channel = channelProvider.get(serverAddress);
        if (channel==null){ // 若为第一次连接或者channel因为不活跃而被删掉，那么重连
            channel=doConnect(serverAddress);
            channelProvider.set(serverAddress,channel);
        }
        return channel;
    }

    @SneakyThrows
    private Channel doConnect(InetSocketAddress serverAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        ChannelFuture future = bootstrap.connect(serverAddress);
        future.addListener(f -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", serverAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                System.out.println(future.cause().getMessage());
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    // 发送连接请求
    public CompletableFuture<RPCResponse<Object>> sendRequest(RPCRequest rpcRequest) {
        // 封装该方法返回的类型
        CompletableFuture<RPCResponse<Object>> resultFuture = new CompletableFuture<>();
        // 获取服务地址
        InetSocketAddress serverAddress = serviceDiscovery.lookupService(rpcRequest);
        // 根据服务地址获取对应的channel
        Channel channel = getChannel(serverAddress);
        if (channel.isActive()) {
        // 放到unprocessedRequests，来保存未返回结果的调用
        unprocessedRequests.put(resultFuture,rpcRequest.getRequestId());
        // 封装RPCMessage，用于传输（其中RPCMessage的requestId在encode里面设置）
        RPCMessage rpcMessage = RPCMessage.builder().messageType(RPCConstants.REQUEST_TYPE)
                .codec(SerializationTypeEnum.KYRO.getCode())
                .compress(CompressTypeEnum.GZIP.getCode())
                .data(rpcRequest).build();
        // 向channel发送消息
        channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                future.channel().close();
                resultFuture.completeExceptionally(future.cause());
                log.error("Send failed:", future.cause());
            }else {
                log.info("client send message: [{}]", rpcMessage);
            }
        });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    // 关闭客户端
    public void close(){
        eventLoopGroup.shutdownGracefully().syncUninterruptibly();
    }
}
