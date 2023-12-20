package github.xuanyunyue.remoting.netty.codec;

import enums.CompressTypeEnum;
import enums.SerializationTypeEnum;
import extension.ExtensionLoader;
import github.xuanyunyue.remoting.compress.Compress;
import github.xuanyunyue.remoting.constants.RPCConstants;
import github.xuanyunyue.remoting.dto.RPCMessage;
import github.xuanyunyue.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author WangTao
 * @author： zyx1128
 * @create： 2023/12/11 15:47
 * @description：TODO <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * @createTime on 2020/10/2
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RPCMessageEncoder extends MessageToByteEncoder<RPCMessage> {//传输的消息就是RPCMessage，所以泛型为RPCMessage

    //=============================这个参数是用来干什么的========================================
    //应该是用于生成requestID的
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RPCMessage msg, ByteBuf out) throws Exception {
        out.writeBytes(RPCConstants.MAGIC_NUMBER);
        out.writeByte(RPCConstants.VERSION);
        out.writerIndex(out.writerIndex() + 4);//先空出4个字节的空间，这4个字节表示full length
        byte messageType = msg.getMessageType();
        out.writeByte(messageType);
        out.writeByte(msg.getCodec());
        out.writeByte(CompressTypeEnum.GZIP.getCode());
        out.writeInt(ATOMIC_INTEGER.getAndIncrement());//RequestId

        // build full length
        byte[] bodyBytes = null;
        int fullLength = RPCConstants.HEAD_LENGTH;

        // 若不是心跳包,fullLength = head length + body length
        if (messageType != RPCConstants.HEARTBEAT_REQUEST_TYPE
                && messageType != RPCConstants.HEARTBEAT_RESPONSE_TYPE) {
            // 序列化对象
            String codecName = SerializationTypeEnum.getName(msg.getCodec());//通过codec的byte来获取codec的编码的名字
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)//创建“实现类”的类加载器，并获取实现类
                    .getExtension(codecName);//这里是通过RPCMessage的codecName来加载了序列化实现类
            bodyBytes = serializer.serialize(msg.getData());//序列化为二进制
            // 压缩序列化后的字节数组
            String compressName = CompressTypeEnum.getName(msg.getCompress());
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bodyBytes = compress.compress(bodyBytes);
            fullLength += bodyBytes.length;
        }

        if (bodyBytes != null) {
            out.writeBytes(bodyBytes);
        }
        //计算5-8的记录full length的长度，并添加
        int writeIndex = out.writerIndex();
        //writeIndex - fullLength=开头，
        // + RPCConstants.MAGIC_NUMBER.length + 1表示跳过魔法数和version长度
        out.writerIndex(writeIndex - fullLength + RPCConstants.MAGIC_NUMBER.length + 1);
        out.writeInt(fullLength);
        out.writerIndex(writeIndex);
    }
}
