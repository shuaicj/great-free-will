package shuaicj.hobby.great.free.will.protocol.socks.message;

import static shuaicj.hobby.great.free.will.protocol.socks.SocksConst.VERSION;

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
     * Decoder of {@link ConnectionResponse}.
     *
     * @author shuaicj 2017/10/12
     */
    @Component
    public static class Decoder implements MessageDecoder<ConnectionResponse> {

        @Autowired ConnectionAddr.Decoder addrDecoder;

        @Override
        public ConnectionResponse decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable(VER_LEN + REP_LEN + RSV_LEN)) {
                return null;
            }
            int mark = in.readerIndex();

            short ver = in.readUnsignedByte();
            if (ver != VERSION) {
                throw new DecoderException("unsupported ver: " + ver);
            }

            ConnectionRep rep = ConnectionRep.valueOf(in.readUnsignedByte());
            short rsv = in.readUnsignedByte();

            ConnectionAddr bnd = addrDecoder.decode(in);
            if (bnd == null) {
                in.readerIndex(mark);
                return null;
            }

            return ConnectionResponse.builder()
                    .ver(ver)
                    .rep(rep)
                    .rsv(rsv)
                    .bnd(bnd)
                    .build();
        }
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
