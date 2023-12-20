package github.xuanyunyue.remoting.dto;

import lombok.*;

/**
 * @author： zyx1128
 * @create： 2023/12/8 10:21
 * @description：网络传输的对象
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RPCMessage {
    //数据类型
    private byte messageType;

    //序列化类型
    private byte codec;

    //压缩类型
    private byte compress;

    //请求id
    private int requestId;

    //传输的数据，分为心跳包和自己定义的RPCRequest/RPCResponse
    private Object data;
}
