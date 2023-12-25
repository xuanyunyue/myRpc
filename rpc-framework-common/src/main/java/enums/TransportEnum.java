package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author： zyx1128
 * @create： 2023/12/25 14:52
 * @description：TODO
 */
@AllArgsConstructor
@Getter
public enum TransportEnum {
    NETTY("netty");

    private final String name;
}
