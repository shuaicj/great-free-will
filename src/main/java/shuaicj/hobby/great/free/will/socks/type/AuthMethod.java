package shuaicj.hobby.great.free.will.socks.type;

import lombok.Getter;

/**
 * SOCKS5 authentication methods.
 *
 *   o  X'00' NO AUTHENTICATION REQUIRED
 *   o  X'01' GSSAPI
 *   o  X'02' USERNAME/PASSWORD
 *   o  X'03' to X'7F' IANA ASSIGNED
 *   o  X'80' to X'FE' RESERVED FOR PRIVATE METHODS
 *   o  X'FF' NO ACCEPTABLE METHODS
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/26
 */
@Getter
public enum AuthMethod {

    NO_AUTHENTICATION_REQUIRED  (0x00),
    GSSAPI                      (0x01),
    USERNAME_PASSWORD           (0x02),
    IANA_ASSIGNED               (0x03, 0x7F),
    RESERVED_FOR_PRIVATE_METHODS(0x80, 0xFE),
    NO_ACCEPTABLE_METHODS       (0xFF);

    private final short valueMin;
    private final short valueMax;

    AuthMethod(int value) {
        this(value, value);
    }

    AuthMethod(int valueMin, int valueMax) {
        this.valueMin = (short) valueMin;
        this.valueMax = (short) valueMax;
    }

    public static AuthMethod valueOf(int value) {
        for (AuthMethod m : AuthMethod.values()) {
            if (m.valueMin <= value && m.valueMax >= value) {
                return m;
            }
        }
        throw new IllegalArgumentException("illegal value: " + value);
    }

    public short value() {
        if (valueMin != valueMax) {
            throw new UnsupportedOperationException("a range");
        }
        return valueMin;
    }
}
