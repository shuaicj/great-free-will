package shuaicj.hobby.great.free.will.protocol.tunnel.message;

import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.BODY_LEN_LEN;
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
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.tunnel.cipher.TunnelCipher;

/**
 * Tunnel connection request. It consists of a 2 byte random salt value, a 2 byte body length value
 * and a socks {@link shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest}.
 *
 *   +-------------+-------------+-------------------+
 *   |     SALT    | BODY_LENGTH |        BODY       |
 *   +-------------+-------------+-------------------+
 *   |      2      |      2      | ConnectionRequest |
 *   +-------------+-------------+-------------------+
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
        return SALT_LEN + BODY_LEN_LEN + body.length();
    }

    /**
     * Decoder of {@link TunnelConnectionRequest}.
     *
     * @author shuaicj 2017/10/11
     */
    @Component
    @Scope("prototype")
    public static class Decoder implements MessageDecoder<TunnelConnectionRequest> {

        @Autowired ConnectionRequest.Decoder bodyDecoder;
        @Autowired TunnelCipher cipher;

        @Override
        public TunnelConnectionRequest decode(ByteBuf in) throws DecoderException {
            try {
                in = cipher.decrypt(in);
            } catch (GeneralSecurityException e) {
                throw new DecoderException(e);
            }

            if (in == null) {
                return null;
            }

            in.readBytes(SALT_LEN + BODY_LEN_LEN); // salt and bodyLength are useless here

            ConnectionRequest body = bodyDecoder.decode(in);
            if (body == null) {
                throw new DecoderException("invalid message after decryption" + in);
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
    @Scope("prototype")
    public static class Encoder implements MessageEncoder<TunnelConnectionRequest> {

        @Autowired ConnectionRequest.Encoder bodyEncoder;
        @Autowired TunnelCipher cipher;

        @Override
        public void encode(TunnelConnectionRequest msg, ByteBuf out) throws EncoderException {
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
