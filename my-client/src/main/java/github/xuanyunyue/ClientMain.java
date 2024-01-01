package github.xuanyunyue;

import github.xuanyunyue.annotations.RPCScan;
import github.xuanyunyue.remoting.transport.netty.client.NettyClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RPCScan(basePackage = {"github.xuanyunyue"})
public class ClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ClientMain.class);
        ExampleController helloController = (ExampleController) applicationContext.getBean("exampleController");
        helloController.test();
    }
}
