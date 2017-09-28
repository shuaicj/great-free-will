package shuaicj.hobby.great.free.will.socks.type;

import lombok.Getter;

/**
 * SOCKS5 connection CMD.
 *
 *   o  CONNECT X'01'
 *   o  BIND X'02'
 *   o  UDP ASSOCIATE X'03'
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/27
 */
@Getter
public enum ConnectionCmd {

    CONNECT      (0x01),
    BIND         (0x02),
    UDP_ASSOCIATE(0x03);

    private final short value;

    ConnectionCmd(int value) {
        this.value = (short) value;
    }

    public static ConnectionCmd valueOf(int value) {
        for (ConnectionCmd m : ConnectionCmd.values()) {
            if (m.value == value) {
                return m;
            }
        }
        throw new IllegalArgumentException("illegal value: " + value);
    }
}
