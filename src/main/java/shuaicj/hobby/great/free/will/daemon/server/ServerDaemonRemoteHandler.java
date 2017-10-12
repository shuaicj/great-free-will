package shuaicj.hobby.great.free.will.daemon.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelDataTransport;
import shuaicj.hobby.great.free.will.util.Utils;

/**
 * A handler used in server daemon to handle data from remote.
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
 * @author shuaicj 2017/10/11
 */
@Component
@Scope("prototype")
@Profile("server")
@Slf4j
public class ServerDaemonRemoteHandler extends ChannelInboundHandlerAdapter {

    private Channel tunnelChannel;
    private Channel remoteChannel;

    public ServerDaemonRemoteHandler(Channel tunnelChannel) {
        this.tunnelChannel = tunnelChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.remoteChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        tunnelChannel.writeAndFlush(
                TunnelDataTransport.builder().body(
                        DataTransport.builder()
                                .data((ByteBuf) msg)
                                .build()).build());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Utils.flushAndClose(tunnelChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("shit happens", e);
        Utils.flushAndClose(remoteChannel);
    }
}
