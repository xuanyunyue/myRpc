package github.xuanyunyue.remoting.handler;

import exception.RPCException;
import factory.SingletonFactory;
import github.xuanyunyue.provider.Impl.ZkServiceProviderImpl;
import github.xuanyunyue.provider.ServiceProvider;
import github.xuanyunyue.remoting.dto.RPCRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author： zyx1128
 * @create： 2023/12/8 11:35
 * @description：TODO 执行客户端的方法调用
 */
@Slf4j
public class RPCRequestHandler {

    //单例
    private final ServiceProvider serviceProvider;

    public RPCRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }


    public Object handle(RPCRequest request) {
        //先获取到服务提供者rpcServiceName（zk里面的名字）
        String rpcServiceName = request.getRPCServiceName();
        //获取对象
        Object service = serviceProvider.getService(rpcServiceName);

        return invokeMethod(service,request);
    }

    private Object invokeMethod(Object service, RPCRequest request) {
        String methodName = request.getMethodName();
        Class<?>[] paramTypes = request.getParamTypes();
        try {
            //getMethod("myMethod") 尝试获取指定名称的公共方法，此处是获取名为 "myMethod" 的方法。
            Method method = service.getClass().getMethod(methodName, paramTypes);
            //method.invoke(obj, "Hello, world!") 将在 obj 对象上调用 myMethod 方法，并传递 "Hello, world!" 作为参数。
            Object res = method.invoke(service, request.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", request.getInterfaceName(), request.getMethodName());
            return res;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RPCException(e.getMessage(),e);
        }
    }
}
