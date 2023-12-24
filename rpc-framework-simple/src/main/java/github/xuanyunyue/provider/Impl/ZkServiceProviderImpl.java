package github.xuanyunyue.provider.Impl;

import enums.RPCErrorMessageEnum;
import exception.RPCException;
import extension.ExtensionLoader;
import github.xuanyunyue.config.RPCServiceConfig;
import github.xuanyunyue.provider.ServiceProvider;
import github.xuanyunyue.registry.ServiceRegistry;
import github.xuanyunyue.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author： zyx1128
 * @create： 2023/12/19 16:36
 * @description：TODO
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;

    //直接操作zk，创建节点
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("ServiceRegistryEnum.ZK.getName()");
    }

    //将服务添加到上面的set和map里,而不是zk里
    @Override
    public void addService(RPCServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (ObjectUtils.isNotEmpty(service)) {
            return service;
        }else {
            throw new RPCException(RPCErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
    }

    //将服务发布到zk上
    @Override
    public void publishService(RPCServiceConfig rpcServiceConfig) {
        try {
            //这个地址是实际网络接口（如以太网、Wi-Fi 等）的 IP 地址，用于与外部网络进行通信。
            String host = InetAddress.getLocalHost().getHostAddress();
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyServer.PORT));
            this.addService(rpcServiceConfig);
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

}
