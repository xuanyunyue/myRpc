package github.xuanyunyue.netty;


import github.xuanyunyue.annotations.RPCScan;
import github.xuanyunyue.config.RPCServiceConfig;
import github.xuanyunyue.remoting.transport.netty.server.NettyServer;
import github.xuanyunyue.ExampleService;
import github.xuanyunyue.ExampleServiceImpl_1;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RPCScan(basePackage = {"github.xuanyunyue"})
public class NettyServerMain {
    public static void main(String[] args) {
        // 通过注释注册服务
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyRpcServer = (NettyServer) applicationContext.getBean("nettyServer");
        // 手动注册服务
        ExampleService exampleService1 = new ExampleServiceImpl_1();
        RPCServiceConfig rpcServiceConfig = RPCServiceConfig.builder()
                .group("test1").version("version1").service(exampleService1).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
