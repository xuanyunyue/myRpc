package github.xuanyunyue.remoting.transport.netty.client;

import github.xuanyunyue.remoting.dto.RPCResponse;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author： zyx1128
 * @create： 2023/12/20 18:36
 * @description：TODO 该类存储没有从server返回的方法调用结果
 * <br>map的映射是使用 CompletableFuture 包装返回结果
 * <br>
 *<br>使用CompletableFuture的优点：
 *<br>  1. 不阻塞 eventloop，保证效率。
 *<br>  2. 不通过连接关闭事件让 eventloop 设置 rpcResponse 和 proxy 所在的线程获取 rpcResposne 同步。
 * 通过自定义的 CompletableFuture 让线程间同步更加合适。
 *
 */
public class UnprocessedRequests {

    // key是requestId，value是封装的RPCResponse
    private static final Map<String, CompletableFuture<RPCResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void complete(RPCResponse<Object> rpcResponse) {
        CompletableFuture<RPCResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (ObjectUtils.isNotEmpty(future)){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException("客户端未找到服务端返回的requestId");
        }
    }

    public void put(CompletableFuture<RPCResponse<Object>> resultFuture, String requestId) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId,resultFuture);
    }
}
