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
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest;

/**
 * Tunnel connection request. It consists of a 2 byte body length value
 * and a socks {@link shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest}.
 *
 *   +-------------+-------------------+
 *   | BODY_LENGTH |        BODY       |
 *   +-------------+-------------------+
 *   |      2      | ConnectionRequest |
 *   +-------------+-------------------+
 *
 * @author shuaicj 2017/10/11
 */
@Getter
@ToString
public class TunnelConnectionRequest implements Message {

    private final ConnectionRequest body;

    @Builder
    private TunnelConnectionRequest(ConnectionRequest body) {
        this.body = body;
    }

    @Override
    public int length() {
        return BODY_LEN_LEN + body.length();
    }

    /**
     * Decoder of {@link TunnelConnectionRequest}.
     *
     * @author shuaicj 2017/10/11
     */
    @Component
    public static class Decoder implements MessageDecoder<TunnelConnectionRequest> {

        @Autowired ConnectionRequest.Decoder bodyDecoder;

        @Override
        public TunnelConnectionRequest decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable(BODY_LEN_LEN)) {
                return null;
            }
            int mark = in.readerIndex();

            int bodyLength = in.readUnsignedShort();
            if (!in.isReadable(bodyLength)) {
                in.readerIndex(mark);
                return null;
            }

            ConnectionRequest body = bodyDecoder.decode(in);
            if (body == null) {
                in.readerIndex(mark);
                return null;
            }

            return TunnelConnectionRequest.builder()
                    .body(body)
                    .build();
        }
    }

    /**
     * Encoder of {@link TunnelConnectionRequest}.
     *
     * @author shuaicj 2017/10/12
     */
    @Component
    public static class Encoder implements MessageEncoder<TunnelConnectionRequest> {

        @Autowired ConnectionRequest.Encoder bodyEncoder;

        @Override
        public void encode(TunnelConnectionRequest msg, ByteBuf out) throws EncoderException {
            out.writeShort(msg.body.length());
            bodyEncoder.encode(msg.body, out);
        }
    }
}
