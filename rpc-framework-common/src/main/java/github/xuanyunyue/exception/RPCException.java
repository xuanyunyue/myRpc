package github.xuanyunyue.exception;

import github.xuanyunyue.enums.RPCErrorMessageEnum;

/**
 * @author： zyx1128
 * @create： 2023/12/18 16:08
 * @description：TODO
 */
public class RPCException extends RuntimeException{
    public RPCException(RPCErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RPCException(String message, Throwable cause) {
        super(message, cause);
    }

    public RPCException(RPCErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
