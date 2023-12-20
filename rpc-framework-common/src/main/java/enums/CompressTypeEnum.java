package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author： zyx1128
 * @create： 2023/12/15 19:03
 * @description：TODO
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
