package shuaicj.hobby.great.free.will.socks.type;

import lombok.Getter;

/**
 * SOCKS5 connection REP.
 *
 *   o  X'00' succeeded
 *   o  X'01' general SOCKS server failure
 *   o  X'02' connection not allowed by ruleset
 *   o  X'03' Network unreachable
 *   o  X'04' Host unreachable
 *   o  X'05' Connection refused
 *   o  X'06' TTL expired
 *   o  X'07' Command not supported
 *   o  X'08' Address type not supported
 *   o  X'09' to X'FF' unassigned
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/09/28
 */
@Getter
public enum ConnectionRep {

    SUCCEEDED                        (0x00),
    GENERAL_SOCKS_SERVER_FAILURE     (0x01),
    CONNECTION_NOT_ALLOWED_BY_RULESET(0x02),
    NETWORK_UNREACHABLE              (0x03),
    HOST_UNREACHABLE                 (0x04),
    CONNECTION_REFUSED               (0x05),
    TTL_EXPIRED                      (0x06),
    COMMAND_NOT_SUPPORTED            (0x07),
    ADDRESS_TYPE_NOT_SUPPORTED       (0x08);

    private final short value;

    ConnectionRep(int value) {
        this.value = (short) value;
    }

    public static ConnectionRep valueOf(int value) {
        for (ConnectionRep m : ConnectionRep.values()) {
            if (m.value == value) {
                return m;
            }
        }
        throw new IllegalArgumentException("illegal value: " + value);
    }
}
