package github.xuanyunyue.remoting.transport.netty.codec;

/**
 * @author： zyx1128
 * @create： 2023/12/8 17:03
 * @description：TODO
 */

import github.xuanyunyue.enums.CompressTypeEnum;
import github.xuanyunyue.enums.SerializationTypeEnum;
import github.xuanyunyue.extension.ExtensionLoader;
import github.xuanyunyue.compress.Compress;
import github.xuanyunyue.remoting.constants.RPCConstants;
import github.xuanyunyue.remoting.dto.RPCMessage;
import github.xuanyunyue.remoting.dto.RPCRequest;
import github.xuanyunyue.remoting.dto.RPCResponse;
import github.xuanyunyue.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 * |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 * +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 * |                                                                                                       |
 * |                                         body                                                          |
 * |                                                                                                       |
 * |                                        ... ...                                                        |
 * +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 */
@Slf4j
public class RPCMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RPCMessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RPCConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength      the maximum length of the frame.  If the length of the frame is
     *                            greater than this value, will be
     *                            thrown.
     * @param lengthFieldOffset   the offset of the length field
     * @param lengthFieldLength   the length of the length field
     * @param lengthAdjustment    the compensation value to add to the value of the length field
     * @param initialBytesToStrip the number of first bytes to strip out from the decoded frame
     */
    public RPCMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param ctx the {@link ChannelHandlerContext} which this belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @return frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     * be created.
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            //解决tcp粘包拆包的问题
            if (frame.readableBytes() >= RPCConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        //校验魔法数和版本号
        checkMagicNumber(in);
        checkVersion(in);

        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        //封装
        RPCMessage rpcMessage = RPCMessage.builder()
                .messageType(messageType)
                .codec(codecType)
                .requestId(requestId).build();
        if (messageType == RPCConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RPCConstants.PONG);
            return rpcMessage;
        }
        if (messageType == RPCConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RPCConstants.PING);
            return rpcMessage;
        }
        int bodyLength = fullLength - RPCConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            //这里对body的内容进行解压和反序列化
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            //解压
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            //反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RPCConstants.REQUEST_TYPE) {
                RPCRequest data = serializer.deserialize(bs, RPCRequest.class);
                rpcMessage.setData(data);
            } else {
                RPCResponse data = serializer.deserialize(bs, RPCResponse.class);
                rpcMessage.setData(data);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RPCConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RPCConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RPCConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
