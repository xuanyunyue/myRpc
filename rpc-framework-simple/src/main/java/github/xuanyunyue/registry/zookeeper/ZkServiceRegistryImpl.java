package github.xuanyunyue.registry.zookeeper;

import github.xuanyunyue.registry.ServiceRegistry;
import github.xuanyunyue.registry.zookeeper.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:21
 * @description：TODO
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        //先拼接在zk下的RPC服务的路径以及post、port
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        //获取java的zk客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //创建节点
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
