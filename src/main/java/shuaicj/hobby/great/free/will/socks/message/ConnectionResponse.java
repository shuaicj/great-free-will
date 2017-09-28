package shuaicj.hobby.great.free.will.socks.message;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import shuaicj.hobby.great.free.will.socks.SocksEncoder;
import shuaicj.hobby.great.free.will.socks.type.ConnectionAddr;
import shuaicj.hobby.great.free.will.socks.type.ConnectionRep;

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
public class ConnectionResponse {

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

    /**
     * Encoder of {@link ConnectionResponse}.
     *
     * @author shuaicj 2017/09/27
     */
    public static class Encoder implements SocksEncoder<ConnectionResponse> {

        @Override
        public void encode(ConnectionResponse msg, ByteBuf out) throws EncoderException {
            out.writeByte(msg.ver);
            out.writeByte(msg.rep.value());
            new ConnectionAddr.Encoder().encode(msg.bnd, out);
        }
    }
}
