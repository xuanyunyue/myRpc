package github.xuanyunyue.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author： zyx1128
 * @create： 2023/12/18 15:41
 * @description：TODO
 */
@AllArgsConstructor
@Getter
public enum RPCConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
