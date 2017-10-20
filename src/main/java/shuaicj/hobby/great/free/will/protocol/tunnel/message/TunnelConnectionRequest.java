package shuaicj.hobby.great.free.will.protocol.tunnel.message;

import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.CIPHER_BUF_LEN;

import java.security.GeneralSecurityException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
 * Tunnel connection request. It is a safe socks message
 * {@link shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest}.
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

    /**
     * Decoder of {@link TunnelConnectionRequest}.
     *
     * @author shuaicj 2017/10/11
     */
    @Component
    @Scope("prototype")
    public static class Decoder implements MessageDecoder<TunnelConnectionRequest> {

        @Autowired ConnectionRequest.Decoder bodyDecoder;

        private final TunnelCipher cipher;

        public Decoder(TunnelCipher cipher) {
            this.cipher = cipher;
        }

        @Override
        public TunnelConnectionRequest decode(ByteBuf in) throws DecoderException {
            int mark = in.readerIndex();

            byte[] buf = new byte[CIPHER_BUF_LEN];
            int len = in.readableBytes();
            in.readBytes(buf, 0, len);
            try {
                cipher.decrypter().doFinal(buf, 0, len, buf);
            } catch (GeneralSecurityException e) {
                throw new DecoderException(e);
            }

            ConnectionRequest body = bodyDecoder.decode(Unpooled.wrappedBuffer(buf, 0, len));
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
    @Scope("prototype")
    public static class Encoder implements MessageEncoder<TunnelConnectionRequest> {

        @Autowired ConnectionRequest.Encoder bodyEncoder;

        private final TunnelCipher cipher;

        public Encoder(TunnelCipher cipher) {
            this.cipher = cipher;
        }

        @Override
        public void encode(TunnelConnectionRequest msg, ByteBuf out) throws EncoderException {
            byte[] buf = new byte[CIPHER_BUF_LEN];
            ByteBuf byteBuf = Unpooled.wrappedBuffer(buf);
            byteBuf.clear();
            bodyEncoder.encode(msg.body, byteBuf);
            int len = byteBuf.readableBytes();
            try {
                cipher.encrypter().doFinal(buf, 0, len, buf);
            } catch (GeneralSecurityException e) {
                throw new EncoderException(e);
            }
            out.writeBytes(byteBuf);
        }
    }
}
