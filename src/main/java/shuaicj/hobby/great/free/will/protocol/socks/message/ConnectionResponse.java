package shuaicj.hobby.great.free.will.protocol.socks.message;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.MessageEncoder;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.socks.message.part.ConnectionAddr;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionRep;

/**
 * SOCKS5 connection response.
 *
 *   +----+-----+-------+------+----------+----------+
 *   |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
 *   +----+-----+-------+------+----------+----------+
 *   | 1  |  1  | X'00' |  1   | Variable |    2     |
 *   +----+-----+-------+------+----------+----------+
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/26
 */
@Getter
@ToString
public class ConnectionResponse implements Message {

    public static final int VER_LEN = 1;
    public static final int REP_LEN = 1;
    public static final int RSV_LEN = 1;

    private final short ver;
    private final ConnectionRep rep;
    private final short rsv;
    private final ConnectionAddr bnd;

    @Builder
    private ConnectionResponse(short ver, ConnectionRep rep, short rsv, ConnectionAddr bnd) {
        this.ver = ver;
        this.rep = rep;
        this.rsv = rsv;
        this.bnd = bnd;
    }

    @Override
    public int length() {
        return VER_LEN + REP_LEN + RSV_LEN + bnd.length();
    }

    /**
     * Encoder of {@link ConnectionResponse}.
     *
     * @author shuaicj 2017/09/27
     */
    @Component
    public static class Encoder implements MessageEncoder<ConnectionResponse> {

        @Autowired ConnectionAddr.Encoder addrEncoder;

        @Override
        public void encode(ConnectionResponse msg, ByteBuf out) throws EncoderException {
            out.writeByte(msg.ver);
            out.writeByte(msg.rep.value());
            out.writeByte(msg.rsv);
            addrEncoder.encode(msg.bnd, out);
        }
    }
}
