package github.xuanyunyue.registry;

import github.xuanyunyue.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:03
 * @description：TODO
 */
@SPI
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
