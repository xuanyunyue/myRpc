package github.xuanyunyue.registry.zookeeper.util;

import enums.RPCConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import utils.PropertiesFileUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:23
 * @description：TODO
 * 该类实现zookeeper以下功能：
 * <br>1. 创建持久化节点。
 * <br>2. 获取某个字节下的子节点,也就是获取所有提供服务的生产者的地址。
 * <br>3. 清空注册中心的数据。
 * <br>4. 注册监听指定节点。
 * <br>
 * <br><a href="https://blog.nowcoder.net/n/6419ec79f3d045d0a9d20edc83bdc097">参考</a>
 */
@Slf4j
public class CuratorUtils {
    //重试之间的间隔时间
    private static final int BASE_SLEEP_TIME = 1000;
    ////最大重试次数
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    //服务名<-->服务地址
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //存放节点
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private CuratorUtils() {
    }

    /**
     *创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除。
     *
     * @param path node path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                //eg: /my-rpc/github.javaguide.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 获取某个字节下的子节点,也就是获取所有提供服务的生产者的地址
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version1
     * @return All child nodes under the specified node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        //若SERVICE_ADDRESS_MAP（缓存）中存在要找的服务名，则返回一个list，里面存放的是address
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            //获取某个节点的所有子节点路径
            result = zkClient.getChildren().forPath(servicePath);
            //添加到map缓存里
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            //注册监听指定节点。
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }
    /**
     * 给节点注册子节点监听器
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            //注册了监听器之后，这个节点的子节点发生变化，这里重新取出子节点的路径并存入SERVICE_ADDRESS_MAP里面
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     - @description: 清空注册中心的数据
     * @param zkClient
     * @param inetSocketAddress
    - @return void
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    //======================没看懂============================
    public static CuratorFramework getZkClient() {
        // check if user has set zk address
        Properties properties = PropertiesFileUtil.readPropertiesFile(RPCConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RPCConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RPCConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

}
