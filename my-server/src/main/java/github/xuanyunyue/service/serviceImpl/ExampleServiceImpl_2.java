package github.xuanyunyue.service.serviceImpl;

import github.xuanyunyue.entity.Example;
import github.xuanyunyue.ExampleService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author： zyx1128
 * @create： 2023/12/26 13:56
 * @description：TODO
 */
@Slf4j
public class ExampleServiceImpl_2 implements ExampleService {
    static {
        System.out.println("ExampleServiceImpl_2被创建");
    }
    @Override
    public String example(Example example) {
        log.info("ExampleServiceImpl_2收到: {}.", example.getMessage());
        String result = example.getDescription();
        log.info("ExampleServiceImpl_2返回: {}.", result);
        return result;
    }
}
