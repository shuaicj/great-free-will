package shuaicj.hobby.great.free.will.protocol.socks.message.part;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.MessageDecoder;
import shuaicj.hobby.great.free.will.protocol.MessageEncoder;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionAddrType;

/**
 /**
 * SOCKS5 connection address. It's usually part of SOCKS messages,
 * eg. {@link shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest}
 * and {@link shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionResponse}
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
@ToString
public class ConnectionAddr implements Message {

    public static final int TYPE_LEN = 1;
    public static final int IP_V4_LEN = 4;
    public static final int IP_V6_LEN = 16;
    public static final int DOMAIN_LEN_LEN = 1;
    public static final int PORT_LEN = 2;

    private final ConnectionAddrType type;
    private final byte[] addr;
    private final int port;

    @Builder
    private ConnectionAddr(ConnectionAddrType type, byte[] addr, int port) {
        this.type = type;
        this.addr = addr;
        this.port = port;
    }

    @Override
    public int length() {
        switch (type) {
            case DOMAIN_NAME:
                return TYPE_LEN + DOMAIN_LEN_LEN + addr.length + PORT_LEN;
            default:
                return TYPE_LEN + addr.length + PORT_LEN;
        }
    }

    /**
     * Decoder of {@link ConnectionAddr}.
     *
     * @author shuaicj 2017/09/28
     */
    @Component
    public static class Decoder implements MessageDecoder<ConnectionAddr> {

        @Override
        public ConnectionAddr decode(ByteBuf in) throws DecoderException {
            if (!in.isReadable()) {
                return null;
            }
            int mark = in.readerIndex();

            ConnectionAddrType type = ConnectionAddrType.valueOf(in.readUnsignedByte());

            byte[] addr = null;
            switch (type) {
                case IP_V4: {
                    if (!in.isReadable(IP_V4_LEN + PORT_LEN)) {
                        in.readerIndex(mark);
                        return null;
                    }
                    addr = new byte[IP_V4_LEN];
                    break;
                }
                case IP_V6: {
                    if (!in.isReadable(IP_V6_LEN + PORT_LEN)) {
                        in.readerIndex(mark);
                        return null;
                    }
                    addr = new byte[IP_V6_LEN];
                    break;
                }
                case DOMAIN_NAME: {
                    if (!in.isReadable(DOMAIN_LEN_LEN)) {
                        in.readerIndex(mark);
                        return null;
                    }
                    int domainLen = in.readUnsignedByte();
                    if (!in.isReadable(domainLen + PORT_LEN)) {
                        in.readerIndex(mark);
                        return null;
                    }
                    addr = new byte[domainLen];
                    break;
                }
            }
            in.readBytes(addr);

            int port = in.readUnsignedShort();

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
    @Component
    public static class Encoder implements MessageEncoder<ConnectionAddr> {

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
