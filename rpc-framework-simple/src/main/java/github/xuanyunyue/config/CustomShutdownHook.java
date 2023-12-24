package github.xuanyunyue.config;

import github.xuanyunyue.registry.zookeeper.util.CuratorUtils;
import github.xuanyunyue.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import utils.concurrent.threadpool.ThreadPoolFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author： zyx1128
 * @create： 2023/12/22 19:07
 * @description：TODO
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyServer.PORT);
                // 删除以本地地址为名字的注册中心
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            ThreadPoolFactory.shutDownAllThreadPool();
        }));
    }
}
