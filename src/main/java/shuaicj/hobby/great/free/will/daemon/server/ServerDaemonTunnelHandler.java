package shuaicj.hobby.great.free.will.daemon.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.socks.SocksConst;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionResponse;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionCmd;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionRep;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelConnectionRequest;
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
 * @author shuaicj 2017/10/11
 */
@Component
@Scope("prototype")
@Profile("server")
@Slf4j
public class ServerDaemonTunnelHandler extends ChannelInboundHandlerAdapter {

    private Channel tunnelChannel;
    private Channel remoteChannel;

    @Autowired private ApplicationContext appCtx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        tunnelChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TunnelConnectionRequest) {
            handleTunnelConnectionRequest((TunnelConnectionRequest) msg);
            return;
        }
        if (msg instanceof TunnelDataTransport) {
            handleTunnelDataTransport((TunnelDataTransport) msg);
            return;
        }
        throw new IllegalStateException("illegal message " + msg);
    }

    private void handleTunnelConnectionRequest(final TunnelConnectionRequest req) throws Exception {
        if (!req.body().cmd().equals(ConnectionCmd.CONNECT)) { // only support TCP CONNECT
            logger.error("unsupported command in request {}", req.body());
            tunnelChannel.writeAndFlush(
                    TunnelConnectionResponse.builder().body(
                            ConnectionResponse.builder()
                                    .ver(SocksConst.VERSION)
                                    .rep(ConnectionRep.COMMAND_NOT_SUPPORTED)
                                    .bnd(req.body().dst())
                                    .build()).build())
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }

        tunnelChannel.config().setAutoRead(false); // disable AutoRead until connection is ready

        Bootstrap b = new Bootstrap();
        b.group(tunnelChannel.eventLoop()) // use the same EventLoop
                .channel(tunnelChannel.getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                appCtx.getBean(LoggingHandler.class),
                                appCtx.getBean(ServerDaemonRemoteHandler.class, tunnelChannel)
                        );
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = b.connect(Utils.inetAddress(req.body()), req.body().dst().port());
        remoteChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                tunnelChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
                tunnelChannel.writeAndFlush(
                        TunnelConnectionResponse.builder().body(
                                ConnectionResponse.builder()
                                        .ver(SocksConst.VERSION)
                                        .rep(ConnectionRep.SUCCEEDED)
                                        .bnd(req.body().dst())
                                        .build()).build());
            } else {
                logger.error("shit happens", future.cause());
                tunnelChannel.writeAndFlush(
                        TunnelConnectionResponse.builder().body(
                                ConnectionResponse.builder()
                                        .ver(SocksConst.VERSION)
                                        .rep(ConnectionRep.HOST_UNREACHABLE)
                                        .bnd(req.body().dst())
                                        .build()).build())
                        .addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    private void handleTunnelDataTransport(TunnelDataTransport in) {
        remoteChannel.writeAndFlush(in.body().data());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Utils.flushAndClose(remoteChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("shit happens", e);
        Utils.flushAndClose(tunnelChannel);
    }
}
