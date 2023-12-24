package github.xuanyunyue.remoting.transport;

import github.xuanyunyue.remoting.dto.RPCRequest;
import github.xuanyunyue.remoting.dto.RPCResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @author： zyx1128
 * @create： 2023/12/22 14:49
 * @description：TODO
 */
public interface SendRequest {
    CompletableFuture<RPCResponse<Object>> sendRequest(RPCRequest rpcRequest);
}
