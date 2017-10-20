package shuaicj.hobby.great.free.will.protocol.socks.message;

import static shuaicj.hobby.great.free.will.protocol.socks.SocksConst.VERSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.MessageDecoder;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.socks.type.AuthMethod;

/**
 * SOCKS5 authentication method selection request.
 *
 *   +----+----------+----------+
 *   |VER | NMETHODS | METHODS  |
 *   +----+----------+----------+
 *   | 1  |    1     | 1 to 255 |
 *   +----+----------+----------+
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/26
 */
@Getter
@ToString
public class AuthMethodRequest implements Message {

    public static final int VER_LEN = 1;
    public static final int NMETHODS_LEN = 1;

    private final short ver;
    private final short nmethods;
    private final Set<AuthMethod> methods;

    @Builder
    private AuthMethodRequest(int ver, int nmethods, Set<AuthMethod> methods) {
        this.ver = (short) ver;
        this.nmethods = (short) nmethods;
        this.methods = Collections.unmodifiableSet(methods);
    }

    /**
     * Decoder of {@link AuthMethodRequest}.
     *
     * @author shuaicj 2017/09/27
     */
    @Component
    public static class Decoder implements MessageDecoder<AuthMethodRequest> {

        @Override
        public AuthMethodRequest decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable(VER_LEN + NMETHODS_LEN)) {
                return null;
            }
            int mark = in.readerIndex();

            short ver = in.readUnsignedByte();
            if (ver != VERSION) {
                throw new DecoderException("unsupported ver: " + ver);
            }

            short nmethods = in.readUnsignedByte();
            if (!in.isReadable(nmethods)) {
                in.readerIndex(mark);
                return null;
            }

            List<AuthMethod> methods = new ArrayList<>();
            for (int i = 0; i < nmethods; i++) {
                short method = in.readUnsignedByte();
                methods.add(AuthMethod.valueOf(method));
            }

            return AuthMethodRequest.builder()
                    .nmethods(nmethods)
                    .methods(EnumSet.copyOf(methods))
                    .build();
        }
    }
}
