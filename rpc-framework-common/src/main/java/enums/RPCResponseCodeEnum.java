package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RPCResponseCodeEnum {
    SUCCESS(200,"remote call succeeded."),
    FAIL(500,"remote call failed");


    private final int code;
    private final String msg;

}
