package shuaicj.hobby.great.free.will.socks.type;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import shuaicj.hobby.great.free.will.socks.SocksDecoder;
import shuaicj.hobby.great.free.will.socks.SocksEncoder;

/**
 /**
 * SOCKS5 connection address. It's usually part of SOCKS messages,
 * eg. {@link shuaicj.hobby.great.free.will.socks.message.ConnectionRequest}
 * and {@link shuaicj.hobby.great.free.will.socks.message.ConnectionResponse}
 *
 *   +------+----------+----------+
 *   | ATYP |   ADDR   |   PORT   |
 *   +------+----------+----------+
 *   |  1   | Variable |    2     |
 *   +------+----------+----------+
 *
 * ATYP field:
 *   o  IP V4 address: X'01'
 *   o  DOMAIN NAME: X'03'
 *   o  IP V6 address: X'04'
 * See {@link ConnectionAddrType}
 *
 * ADDR field:
 *   o  X'01' the address is a version-4 IP address, with a length of 4 octets
 *   o  X'03' the address field contains a fully-qualified domain name.  The first
 *            octet of the address field contains the number of octets of name that
 *            follow, there is no terminating NUL octet.
 *   o  X'04' the address is a version-6 IP address, with a length of 16 octets.
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/28
 */
@Getter
public class ConnectionAddr {

    public static final int TYPE_SIZE = 1;
    public static final int IP_V4_SIZE = 4;
    public static final int IP_V6_SIZE = 16;
    public static final int DOMAIN_LEN_SIZE = 1;
    public static final int PORT_SIZE = 2;

    private final ConnectionAddrType type;
    private final byte[] addr;
    private final int port;

    @Builder
    private ConnectionAddr(ConnectionAddrType type, byte[] addr, int port) {
        this.type = type;
        this.addr = addr;
        this.port = port;
    }

    /**
     * Decoder of {@link ConnectionAddr}.
     *
     * @author shuaicj 2017/09/28
     */
    public static class Decoder implements SocksDecoder<ConnectionAddr> {

        @Override
        public ConnectionAddr decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable()) {
                return null;
            }
            int start = in.readerIndex();

            ConnectionAddrType type = ConnectionAddrType.valueOf(in.getUnsignedByte(start));
            byte[] addr = null;
            int port = 0;
            switch (type) {
                case IP_V4: {
                    if (!in.isReadable(TYPE_SIZE + IP_V4_SIZE + PORT_SIZE)) {
                        return null;
                    }
                    addr = new byte[IP_V4_SIZE];
                    in.getBytes(start + TYPE_SIZE, addr);
                    port = in.getUnsignedShort(start + TYPE_SIZE + IP_V4_SIZE);
                    in.readerIndex(start + TYPE_SIZE + IP_V4_SIZE + PORT_SIZE);
                    break;
                }
                case IP_V6: {
                    if (!in.isReadable(TYPE_SIZE + IP_V6_SIZE + PORT_SIZE)) {
                        return null;
                    }
                    addr = new byte[IP_V6_SIZE];
                    in.getBytes(start + TYPE_SIZE, addr);
                    port = in.getUnsignedShort(start + TYPE_SIZE + IP_V6_SIZE);
                    in.readerIndex(start + TYPE_SIZE + IP_V6_SIZE + PORT_SIZE);
                    break;
                }
                case DOMAIN_NAME: {
                    if (!in.isReadable(TYPE_SIZE + DOMAIN_LEN_SIZE)) {
                        return null;
                    }
                    int domainLen = in.getUnsignedByte(start + TYPE_SIZE);
                    if (!in.isReadable(TYPE_SIZE + DOMAIN_LEN_SIZE + domainLen + PORT_SIZE)) {
                        return null;
                    }
                    addr = new byte[domainLen];
                    in.getBytes(start + TYPE_SIZE + DOMAIN_LEN_SIZE, addr);
                    port = in.getUnsignedShort(start + TYPE_SIZE + DOMAIN_LEN_SIZE + domainLen);
                    in.readerIndex(start + TYPE_SIZE + DOMAIN_LEN_SIZE + domainLen + PORT_SIZE);
                    break;
                }
            }

            return ConnectionAddr.builder()
                    .type(type)
                    .addr(addr)
                    .port(port)
                    .build();
        }
    }

    /**
     * Encoder of {@link ConnectionAddr}.
     *
     * @author shuaicj 2017/09/28
     */
    public static class Encoder implements SocksEncoder<ConnectionAddr> {

        @Override
        public void encode(ConnectionAddr msg, ByteBuf out) throws EncoderException {
            out.writeByte(msg.type.value());
            switch (msg.type) {
                case IP_V4:
                case IP_V6:
                    out.writeBytes(msg.addr);
                    break;
                case DOMAIN_NAME:
                    out.writeByte(msg.addr.length);
                    out.writeBytes(msg.addr);
            }
            out.writeShort(msg.port);
        }
    }
}
