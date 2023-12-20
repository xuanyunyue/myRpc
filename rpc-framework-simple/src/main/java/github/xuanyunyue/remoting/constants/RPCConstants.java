package github.xuanyunyue.remoting.constants;

/**
 * @author： zyx1128
 * @create： 2023/12/8 10:39
 * @description：TODO
 */
public class RPCConstants {
    //请求响应的msg type
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //心跳包相关
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final String PING = "ping";
    public static final String PONG = "pong";


    /*
    通常是 4 个字节。这个魔数主要是为了筛选来到服务端的数据包。
    服务端首先取出前面四个字节进行比对，能够在第一时间识别出这个数据包并非是遵循自定义协议的，也就是无效数据包，为了安全考虑可以直接关闭连接以节省资源。
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final byte VERSION = 1;
    //每个消息的总长
    public static final byte TOTAL_LENGTH = 16;
    //不知道
    public static final int HEAD_LENGTH = 16;
    //不知道
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
