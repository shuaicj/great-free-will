package shuaicj.hobby.great.free.will.protocol.socks.message;

import static shuaicj.hobby.great.free.will.protocol.socks.SocksConst.VERSION;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.MessageDecoder;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.socks.message.part.ConnectionAddr;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionCmd;

/**
 * SOCKS5 connection request.
 *
 *   +----+-----+-------+------+----------+----------+
 *   |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
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
public class ConnectionRequest implements Message {

    public static final int VER_LEN = 1;
    public static final int CMD_LEN = 1;
    public static final int RSV_LEN = 1;

    private final short ver;
    private final ConnectionCmd cmd;
    private final short rsv;
    private final ConnectionAddr dst;

    @Builder
    private ConnectionRequest(short ver, ConnectionCmd cmd, short rsv, ConnectionAddr dst) {
        this.ver = ver;
        this.cmd = cmd;
        this.rsv = rsv;
        this.dst = dst;
    }

    @Override
    public int length() {
        return VER_LEN + CMD_LEN + RSV_LEN + dst.length();
    }

    /**
     * Decoder of {@link ConnectionRequest}.
     *
     * @author shuaicj 2017/09/27
     */
    @Component
    public static class Decoder implements MessageDecoder<ConnectionRequest> {

        @Autowired ConnectionAddr.Decoder addrDecoder;

        @Override
        public ConnectionRequest decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable(VER_LEN + CMD_LEN + RSV_LEN)) {
                return null;
            }
            int mark = in.readerIndex();

            short ver = in.readUnsignedByte();
            if (ver != VERSION) {
                throw new DecoderException("unsupported ver: " + ver);
            }

            ConnectionCmd cmd = ConnectionCmd.valueOf(in.readUnsignedByte());
            short rsv = in.readUnsignedByte();

            ConnectionAddr dst = addrDecoder.decode(in);
            if (dst == null) {
                in.readerIndex(mark);
                return null;
            }

            return ConnectionRequest.builder()
                    .ver(ver)
                    .cmd(cmd)
                    .rsv(rsv)
                    .dst(dst)
                    .build();
        }
    }
}
