package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author： zyx1128
 * @create： 2023/12/21 21:35
 * @description：TODO
 */
@AllArgsConstructor
@Getter
public enum ServiceDiscoveryEnum {
    ZK("zk");


    private final String name;

}
