package shuaicj.hobby.great.free.will.protocol.socks.message;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.MessageEncoder;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.socks.type.AuthMethod;

/**
 * SOCKS5 authentication method selection response.
 *
 *   +----+--------+
 *   |VER | METHOD |
 *   +----+--------+
 *   | 1  |   1    |
 *   +----+--------+
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/26
 */
@Getter
@ToString
public class AuthMethodResponse implements Message {

    public static final int VER_LEN = 1;
    public static final int METHOD_LEN = 1;

    private final short ver;
    private final AuthMethod method;

    @Builder
    private AuthMethodResponse(int ver, AuthMethod method) {
        this.ver = (short) ver;
        this.method = method;
    }

    /**
     * Encoder of {@link AuthMethodResponse}.
     *
     * @author shuaicj 2017/09/27
     */
    @Component
    public static class Encoder implements MessageEncoder<AuthMethodResponse> {

        @Override
        public void encode(AuthMethodResponse msg, ByteBuf out) throws EncoderException {
            out.writeByte(msg.ver);
            out.writeByte(msg.method.value());
        }
    }
}
