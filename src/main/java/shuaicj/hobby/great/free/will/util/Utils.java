package shuaicj.hobby.great.free.will.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionAddrType;

/**
 * Common utils.
 *
 * @author shuaicj 2017/10/09
 */
public class Utils {

    public static void flushAndClose(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static InetAddress inetAddress(ConnectionRequest req) throws UnknownHostException {
        if (req.dst().type().equals(ConnectionAddrType.DOMAIN_NAME)) {
            return InetAddress.getByName(new String(req.dst().addr())); // domain name
        }
        return InetAddress.getByAddress(req.dst().addr()); // IPv4, IPv6
    }
}
