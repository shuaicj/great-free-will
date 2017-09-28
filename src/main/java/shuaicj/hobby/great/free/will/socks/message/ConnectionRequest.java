package shuaicj.hobby.great.free.will.socks.message;

import static shuaicj.hobby.great.free.will.socks.SocksConst.VERSION;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.socks.SocksDecoder;
import shuaicj.hobby.great.free.will.socks.SocksMessage;
import shuaicj.hobby.great.free.will.socks.type.ConnectionAddr;
import shuaicj.hobby.great.free.will.socks.type.ConnectionCmd;

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
public class ConnectionRequest implements SocksMessage {

    public static final int VER_SIZE = 1;
    public static final int CMD_SIZE = 1;
    public static final int RSV_SIZE = 1;

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

    /**
     * Decoder of {@link ConnectionRequest}.
     *
     * @author shuaicj 2017/09/27
     */
    @Component
    public static class Decoder implements SocksDecoder<ConnectionRequest> {

        @Autowired ConnectionAddr.Decoder addrDecoder;

        @Override
        public ConnectionRequest decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable(VER_SIZE + CMD_SIZE + RSV_SIZE)) {
                return null;
            }

            in.markReaderIndex();

            short ver = in.readUnsignedByte();
            if (ver != VERSION) {
                throw new DecoderException("unsupported ver: " + ver);
            }

            ConnectionCmd cmd = ConnectionCmd.valueOf(in.readUnsignedByte());
            short rsv = in.readUnsignedByte();

            ConnectionAddr dst = addrDecoder.decode(in);
            if (dst == null) {
                in.resetReaderIndex();
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
