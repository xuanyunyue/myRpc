package github.xuanyunyue;

import github.xuanyunyue.annotations.RPCService;
import github.xuanyunyue.entity.Example;
import lombok.extern.slf4j.Slf4j;

/**
 * @author： zyx1128
 * @create： 2023/12/26 13:56
 * @description：TODO
 */
@Slf4j
@RPCService(group = "group1", version = "version1")
public class ExampleServiceImpl_1 implements ExampleService {
    static {
        System.out.println("ExampleServiceImpl_1被创建");
    }
    @Override
    public String example(Example example) {
        log.info("ExampleServiceImpl_1收到: {}.", example.getMessage());
        String result = example.getDescription();
        log.info("ExampleServiceImpl_1返回: {}.", result);
        return result;
    }
}
