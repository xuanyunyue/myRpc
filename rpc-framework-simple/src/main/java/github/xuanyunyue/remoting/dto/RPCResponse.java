package github.xuanyunyue.remoting.dto;

import github.xuanyunyue.enums.RPCResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RPCResponse<T> implements Serializable {
    private static final long serialVersionUID = 2L;
    private String requestId;
//    状态码
    private Integer code;
//    msg
    private String message;
//    body
    private T data;


//    供其他方法使用，成功的返回
    public static <T> RPCResponse<T> success(T data, String requestId) {
//        生成一个RPCResponse，并添加成功的code和msg
        RPCResponse<T> response = new RPCResponse<>();
        response.setCode(RPCResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RPCResponseCodeEnum.SUCCESS.getMsg());
        response.setRequestId(requestId);
//        如果data为空，就不添加data了
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RPCResponse<T> fail(RPCResponseCodeEnum rpcResponseCodeEnum) {
        RPCResponse<T> response = new RPCResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMsg());
        return response;
    }


}
