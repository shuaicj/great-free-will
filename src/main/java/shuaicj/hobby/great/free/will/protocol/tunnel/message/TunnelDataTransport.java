package shuaicj.hobby.great.free.will.protocol.tunnel.message;

import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.BODY_LEN_LEN;
import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.BODY_LEN_MAX;
import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.SALT_LEN;
import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.SALT_MAX;

import java.security.GeneralSecurityException;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.MessageDecoder;
import shuaicj.hobby.great.free.will.protocol.MessageEncoder;
import shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport;
import shuaicj.hobby.great.free.will.protocol.tunnel.cipher.TunnelCipher;

/**
 /**
 * Tunnel data transport. It consists of a 2 byte random salt value, a 2 byte body length value
 * and a socks {@link shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport}.
 *
 *   +-------------+-------------+-------------------+
 *   |     SALT    | BODY_LENGTH |        BODY       |
 *   +-------------+-------------+-------------------+
 *   |      2      |      2      |   DataTransport   |
 *   +-------------+-------------+-------------------+
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
        return SALT_LEN + BODY_LEN_LEN + body.length();
    }

    /**
     * Decoder of {@link TunnelDataTransport}.
     *
     * @author shuaicj 2017/10/12
     */
    @Component
    @Scope("prototype")
    public static class Decoder implements MessageDecoder<TunnelDataTransport> {

        @Autowired DataTransport.Decoder bodyDecoder;
        @Autowired TunnelCipher cipher;

        @Override
        public TunnelDataTransport decode(ByteBuf in) throws DecoderException {
            try {
                in = cipher.decrypt(in);
            } catch (GeneralSecurityException e) {
                throw new DecoderException(e);
            }

            if (in == null) {
                return null;
            }

            in.readBytes(SALT_LEN + BODY_LEN_LEN); // salt and bodyLength are useless here

            DataTransport body = bodyDecoder.decode(in);
            if (body == null) {
                throw new DecoderException("invalid message after decryption" + in);
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
    @Scope("prototype")
    public static class Encoder implements MessageEncoder<TunnelDataTransport> {

        @Autowired DataTransport.Encoder bodyEncoder;
        @Autowired TunnelCipher cipher;

        @Override
        public void encode(TunnelDataTransport msg, ByteBuf out) throws EncoderException {
            while (msg.body.length() > BODY_LEN_MAX) {
                realEncode(TunnelDataTransport.builder().body(
                        DataTransport.builder()
                                .data(msg.body.data().readBytes(BODY_LEN_MAX))
                                .build()).build(), out);
            }
            realEncode(msg, out);
        }

        private void realEncode(TunnelDataTransport msg, ByteBuf out) throws EncoderException {
            ByteBuf buf = out.alloc().buffer(msg.length());
            buf.writeShort((int) (Math.random() * (SALT_MAX + 1)));
            buf.writeShort(msg.body.length());
            bodyEncoder.encode(msg.body, buf);
            try {
                buf = cipher.encrypt(buf);
            } catch (GeneralSecurityException e) {
                throw new EncoderException(e);
            }
            out.writeBytes(buf);
        }
    }
}
