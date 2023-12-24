package github.xuanyunyue.remoting.transport.netty.client;

import io.netty.channel.Channel;
import org.apache.commons.lang3.ObjectUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author： zyx1128
 * @create： 2023/12/21 16:41
 * @description：TODO 存放客户端和服务端连接的channel
 */
public class ChannelProvider {
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }


    public Channel get(InetSocketAddress serverAddress) {
        // 获取服务器的ip
        String key = serverAddress.toString();
        if (channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            // 如果channel活跃就返回，不活跃就从map里删掉并返回null
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress serverAddress, Channel channel) {

    }
}
