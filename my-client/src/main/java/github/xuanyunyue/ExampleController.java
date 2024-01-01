package github.xuanyunyue;

import github.xuanyunyue.annotations.RPCReference;
import org.springframework.stereotype.Component;

/**
 * @author： zyx1128
 * @create： 2023/12/26 14:25
 * @description：TODO
 */
@Component
public class ExampleController {

    @RPCReference(version = "version1", group = "test1")
    private ExampleService exampleService;

    public void test() throws InterruptedException {
        String hello = this.exampleService.example(new Example("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        // assert "Hello description is 222".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            System.out.println(exampleService.example(new Example("111", "222")));
        }
    }

}
