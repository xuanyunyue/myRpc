package github.xuanyunyue.config;

import lombok.*;

/**
 * @author： zyx1128
 * @create： 2023/12/19 16:21
 * @description：TODO
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RPCServiceConfig {

    private String version = "";

    //当接口有多个实现类时，按组区分
    private String group = "";

    //目标服务
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /*
    getClass() 方法返回对象的运行时类 Class 对象。
    getInterfaces() 方法返回一个数组，其中包含了这个类所实现的所有接口。
    [0] 表示访问数组中的第一个元素，也就是该类实现的第一个接口。
    getCanonicalName() 方法用于获取规范名称，即接口的全限定名。
     */
    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
