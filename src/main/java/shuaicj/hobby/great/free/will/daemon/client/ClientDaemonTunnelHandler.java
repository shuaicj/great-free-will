package shuaicj.hobby.great.free.will.daemon.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelConnectionResponse;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelDataTransport;
import shuaicj.hobby.great.free.will.util.Utils;

/**
 * A handler used in client daemon to handle data from the safe tunnel.
 *
 *                                           |
 *                                           |
 *                                           |
 *               YOUR PC                     |            YOUR SERVER
 *    _____________________________                     _______________              ____________
 *   |                             |    SAFE TUNNEL    |               |            |            |
 *   | native <----> client daemon | <---------------> | server daemon | <--------> |   remote   |
 *   |_____________________________|                   |_______________|            |____________|
 *                                           |
 *                                           |
 *                                           |
 *                                           |
 *                                        THE WALL
 *
 * @author shuaicj 2017/10/10
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonTunnelHandler extends ChannelInboundHandlerAdapter {

    private Channel nativeChannel;
    private Channel tunnelChannel;

    public ClientDaemonTunnelHandler(Channel nativeChannel) {
        this.nativeChannel = nativeChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.tunnelChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof TunnelConnectionResponse) {
            nativeChannel.writeAndFlush(((TunnelConnectionResponse) msg).body());
            return;
        }
        if (msg instanceof TunnelDataTransport) {
            nativeChannel.writeAndFlush(((TunnelDataTransport) msg).body());
            return;
        }
        throw new IllegalStateException("illegal message " + msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Utils.flushAndClose(nativeChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("shit happens", e);
        Utils.flushAndClose(tunnelChannel);
    }
}
