package github.xuanyunyue.registry;

import java.net.InetSocketAddress;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:03
 * @description：TODO
 */
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
