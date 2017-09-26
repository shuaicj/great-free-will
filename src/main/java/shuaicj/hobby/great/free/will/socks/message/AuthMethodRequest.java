package shuaicj.hobby.great.free.will.socks.message;

import static shuaicj.hobby.great.free.will.socks.Const.NMETHODS_SIZE;
import static shuaicj.hobby.great.free.will.socks.Const.VER_SIZE;
import static shuaicj.hobby.great.free.will.socks.Const.VER_VALUE;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

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
public class AuthMethodRequest {

    private short ver;
    private short nmethods;
    private short[] methods;

    public static AuthMethodRequest parse(ByteBuf buf) {
        if (buf.readableBytes() < VER_SIZE + NMETHODS_SIZE) {
            return null;
        }

        buf.markReaderIndex();

        short ver = buf.readUnsignedByte();
        if (ver != VER_VALUE) {
            buf.resetReaderIndex();
            throw new IllegalArgumentException("illegal ver: " + ver);
        }

        short nmethods = buf.readUnsignedByte();
        if (buf.readableBytes() < nmethods) {
            buf.resetReaderIndex();
            return null;
        }

        for (int i = 0; i < nmethods; i++) {
            short method = buf.readUnsignedByte();
        }

        return null;
    }
}
