package github.xuanyunyue.remoting.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class RPCRequest implements Serializable {
//    序列号UID,以验证加载的类和序列化的对象是否兼容。
    /**
     * 序列化号 serialVersionUID 属于版本控制的作用。反序列化时，会检查 serialVersionUID 是否和当前类的 serialVersionUID 一致。
     * 如果 serialVersionUID 不一致则会抛出 InvalidClassException 异常。
     *相当于类的ID，这个类序列化存入到文件后，反序列化回来需要通过这个serialVersionUID来判断是不是接收的类
     */
    private static final long serialVersionUID=1L;
    private String requestId;
//    请求的接口
    private String interfaceName;
//    请求方法
    private String methodName;
//    参数
    private Object[] parameters;
//    参数类型
    private Class<?>[] paramTypes;
//    当前服务版本
    private String version;
//    处理一个接口有多个类实现的情况？
    private String group;
    public String getRPCServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
