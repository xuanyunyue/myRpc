package github.xuanyunyue.registry;

import github.xuanyunyue.remoting.dto.RPCRequest;

import java.net.InetSocketAddress;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:02
 * @description：TODO
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RPCRequest rpcRequest);
}
