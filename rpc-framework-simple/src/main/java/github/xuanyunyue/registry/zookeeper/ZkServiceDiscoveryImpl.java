package github.xuanyunyue.registry.zookeeper;

import github.xuanyunyue.enums.LoadBalanceEnum;
import github.xuanyunyue.enums.RPCErrorMessageEnum;
import github.xuanyunyue.exception.RPCException;
import github.xuanyunyue.extension.ExtensionLoader;
import github.xuanyunyue.loadBalance.LoadBalance;
import github.xuanyunyue.registry.ServiceDiscovery;
import github.xuanyunyue.registry.zookeeper.util.CuratorUtils;
import github.xuanyunyue.remoting.dto.RPCRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:20
 * @description：TODO
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    //根据完整的服务名称便可以将对应的服务地址查出来， 查出来的服务地址可能并不止一个。
    //所以可以通过对应的负载均衡策略来选择出一个服务地址。
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    /**
     * 根据 rpcServiceName 获取远程服务地址
     * @param rpcRequest
     * @return InetSocketAddress
     */
    @Override
    public InetSocketAddress lookupService(RPCRequest rpcRequest) {
        //获取rpcServiceName
        String rpcServiceName = rpcRequest.getRPCServiceName();
        //获取zk的java客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //通过该客户端得到rpcServiceName路径下的地址
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtils.isEmpty(serviceUrlList)) {
            throw new RPCException(RPCErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 通过负载均衡选一个地址来调用服务
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
