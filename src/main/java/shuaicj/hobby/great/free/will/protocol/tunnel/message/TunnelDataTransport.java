package shuaicj.hobby.great.free.will.protocol.tunnel.message;

import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.BODY_LEN_LEN;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.MessageDecoder;
import shuaicj.hobby.great.free.will.protocol.MessageEncoder;
import shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport;

/**
 /**
 * Tunnel connection request. It consists of a 4 byte body length value
 * and a socks {@link shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport}.
 *
 *   +-------------+-------------------+
 *   | BODY_LENGTH |        BODY       |
 *   +-------------+-------------------+
 *   |      4      |   DataTransport   |
 *   +-------------+-------------------+
 *
 *
 * @author shuaicj 2017/10/12
 */
@Getter
@ToString
public class TunnelDataTransport implements Message {

    private final DataTransport body;

    @Builder
    private TunnelDataTransport(DataTransport body) {
        this.body = body;
    }

    @Override
    public int length() {
        return BODY_LEN_LEN + body.length();
    }

    /**
     * Decoder of {@link TunnelDataTransport}.
     *
     * @author shuaicj 2017/10/12
     */
    @Component
    public static class Decoder implements MessageDecoder<TunnelDataTransport> {

        @Autowired DataTransport.Decoder bodyDecoder;

        @Override
        public TunnelDataTransport decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable(BODY_LEN_LEN)) {
                return null;
            }
            int mark = in.readerIndex();

            int bodyLength = (int) in.readUnsignedInt();
            if (!in.isReadable(bodyLength)) {
                in.readerIndex(mark);
                return null;
            }

            DataTransport body = bodyDecoder.decode(in.readBytes(bodyLength));
            if (body == null) {
                in.readerIndex(mark);
                return null;
            }

            return TunnelDataTransport.builder()
                    .body(body)
                    .build();
        }
    }

    /**
     * Encoder of {@link TunnelDataTransport}.
     *
     * @author shuaicj 2017/10/12
     */
    @Component
    public static class Encoder implements MessageEncoder<TunnelDataTransport> {

        @Autowired DataTransport.Encoder bodyEncoder;

        @Override
        public void encode(TunnelDataTransport msg, ByteBuf out) throws EncoderException {
            out.writeInt(msg.body.length());
            bodyEncoder.encode(msg.body, out);
        }
    }
}
