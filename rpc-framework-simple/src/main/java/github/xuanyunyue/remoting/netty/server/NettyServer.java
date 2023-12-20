package github.xuanyunyue.remoting.netty.server;

import factory.SingletonFactory;
import github.xuanyunyue.config.RPCServiceConfig;
import github.xuanyunyue.provider.Impl.ZkServiceProviderImpl;
import github.xuanyunyue.provider.ServiceProvider;
import github.xuanyunyue.remoting.netty.codec.RPCMessageDecoder;
import github.xuanyunyue.remoting.netty.codec.RPCMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import utils.concurrent.threadpool.ThreadPoolFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyServer {
    public static final int PORT = 9998;
    private static final String HOST = "127.0.0.1";

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    //注册方法到provider到zk上
    public void registerService(RPCServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        //关闭所有客户端连接...

        //server-bootstrap的准备
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建一个业务线程池，pipeline的handler用专门的线程处理，和主事件循环的线程分离开
        //将业务的ChannelHandler与具体的DefaultEventExecutor绑定，就可以从该线程池中分配线程去处理该处理器中的业务。
        DefaultEventExecutorGroup serverHandlerGroup = new DefaultEventExecutorGroup(
                //线程池线程个数
                Runtime.getRuntime().availableProcessors() * 2,
                //线程池
                ThreadPoolFactory.createThreadFactory("service-handler-group", false)
        );

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //childOption:设置TCP连接中的一些可选项,而且这些属性是作用于每一个连接到服务器被创建的channel。
                    //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //开启tcp底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //日志handler
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //netty的心跳，处理空闲状态的处理器
                            //IdleStateHandler触发后，会传递给管道的下一个handler处理
                            //三个参数分别为：  1. x秒没有读时，就发送一个心跳检测包检测是否连接
                            //              2. x秒没有写时，就发送一个心跳检测包检测是否连接
                            //              3. x秒没有读or写时，就发送一个心跳检测包检测是否连接
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            //出站的编码器
                            pipeline.addLast(new RPCMessageEncoder());
                            //入站的解码器
                            pipeline.addLast(new RPCMessageDecoder());
                            //处理自己的业务，同时加上先前创建好的线程池
                            pipeline.addLast(serverHandlerGroup,new NettyServerHandler() );
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(HOST, PORT));
//        等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("there is an error on github.xuanyunyue.remoting.netty.server.NettyServer's start");
        } finally {
            log.error("Netty's server has shutdown");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serverHandlerGroup.shutdownGracefully();
        }

    }
}
