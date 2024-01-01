package github.xuanyunyue.proxy;

import github.xuanyunyue.enums.RPCErrorMessageEnum;
import github.xuanyunyue.enums.RPCResponseCodeEnum;
import github.xuanyunyue.exception.RPCException;
import github.xuanyunyue.config.RPCServiceConfig;
import github.xuanyunyue.remoting.dto.RPCRequest;
import github.xuanyunyue.remoting.dto.RPCResponse;
import github.xuanyunyue.remoting.transport.SendRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author： zyx1128
 * @create： 2023/12/22 14:31
 * @description：TODO 动态代理类。
 * 当动态代理对象调用一个方法时，它实际上调用下面的invoke方法。正是由于动态代理，客户端调用的远程方法就像调用本地方法一样(中间进程被屏蔽)。
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final SendRequest sendRequest;
    private final RPCServiceConfig rpcServiceConfig;

    public RpcClientProxy(SendRequest target, RPCServiceConfig rpcServiceConfig) {
        this.sendRequest = target;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    // 这里没有用method.invoke()是因为我们还需要处理其他数据，需要知道这个方法返回的是RRCResponse类，而method.invoke()获得的结果是SendRequest返回类型
    // 而实例想要的功能是，仅仅返回PRCResponse的data，所以在这个方法里获取了RPCResponse的data并返回
    // 这样客户端就只获得了data，而不是RPCResponse
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("RPC client start call remote service, invoked method: [{}]", method.getName());
        // 封装消息体
        RPCRequest rpcRequest = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .parameters(args)
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion()).build();
        CompletableFuture<RPCResponse<Object>> resFuture = sendRequest.sendRequest(rpcRequest);

        // 因为只用了netty，所以就不跟示例一样判断sendRequest是哪个类型了
        RPCResponse<Object> rpcResponse = resFuture.get();
        // 校验返回的rpcResponse
        check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    //校验
    private void check(RPCResponse<Object> rpcResponse, RPCRequest rpcRequest) {
        // 先看rpcResponse是否为空
        if (rpcResponse == null) {
            throw new RPCException(RPCErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "接口名称:" + rpcRequest.getInterfaceName());
        }
        // 再看requestId是否一致
        if (!StringUtils.equals(rpcRequest.getRequestId(), rpcResponse.getRequestId())) {
            throw new RPCException(RPCErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, "接口名称:" + rpcRequest.getInterfaceName());
        }
        // 最后校验rpcResponse里面的状态码
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RPCResponseCodeEnum.SUCCESS.getCode())) {
            throw new RPCException(RPCErrorMessageEnum.SERVICE_INVOCATION_FAILURE,"接口名称:" + rpcRequest.getInterfaceName());
        }

    }

    /**
     * get the proxy object
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }
}
