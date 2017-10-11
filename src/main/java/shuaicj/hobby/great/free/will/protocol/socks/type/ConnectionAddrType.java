package shuaicj.hobby.great.free.will.protocol.socks.type;

import lombok.Getter;

/**
 * SOCKS5 connection address type.
 *
 *   o  IP V4 address: X'01'
 *   o  DOMAIN NAME: X'03'
 *   o  IP V6 address: X'04'
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/27
 */
@Getter
public enum ConnectionAddrType {

    IP_V4      (0x01),
    DOMAIN_NAME(0x03),
    IP_V6      (0x04);

    private final short value;

    ConnectionAddrType(int value) {
        this.value = (short) value;
    }

    public static ConnectionAddrType valueOf(int value) {
        for (ConnectionAddrType m : ConnectionAddrType.values()) {
            if (m.value == value) {
                return m;
            }
        }
        throw new IllegalArgumentException("illegal value: " + value);
    }
}
