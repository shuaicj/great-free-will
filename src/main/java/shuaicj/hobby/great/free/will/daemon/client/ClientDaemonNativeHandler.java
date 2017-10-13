package shuaicj.hobby.great.free.will.daemon.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.socks.SocksConst;
import shuaicj.hobby.great.free.will.protocol.socks.message.AuthMethodRequest;
import shuaicj.hobby.great.free.will.protocol.socks.message.AuthMethodResponse;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionResponse;
import shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport;
import shuaicj.hobby.great.free.will.protocol.socks.type.AuthMethod;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionRep;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelDataTransport;
import shuaicj.hobby.great.free.will.util.Utils;

/**
 * A handler used in client daemon to handle data from native.
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
 * @author shuaicj 2017/10/09
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonNativeHandler extends ChannelInboundHandlerAdapter {

    private Channel nativeChannel;
    private Channel tunnelChannel;

    @Value("${server.daemon.host}") private String serverDaemonHost;
    @Value("${server.daemon.port}") private int serverDaemonPort;
    @Autowired private ApplicationContext appCtx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        nativeChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof AuthMethodRequest) {
            handleAuthMethodRequest((AuthMethodRequest) msg);
            return;
        }
        if (msg instanceof ConnectionRequest) {
            handleConnectionRequest((ConnectionRequest) msg);
            return;
        }
        if (msg instanceof DataTransport) {
            handleDataTransport((DataTransport) msg);
            return;
        }
        throw new IllegalStateException("illegal message " + msg);
    }

    private void handleAuthMethodRequest(AuthMethodRequest req) {
        if (!req.methods().contains(AuthMethod.NO_AUTHENTICATION_REQUIRED)) { // only support NO_AUTHENTICATION_REQUIRED
            logger.error("no acceptable auth method in request {}", req);
            nativeChannel.writeAndFlush(
                    AuthMethodResponse.builder()
                            .ver(SocksConst.VERSION)
                            .method(AuthMethod.NO_ACCEPTABLE_METHODS)
                            .build())
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        nativeChannel.writeAndFlush(
                AuthMethodResponse.builder()
                        .ver(SocksConst.VERSION)
                        .method(AuthMethod.NO_AUTHENTICATION_REQUIRED)
                        .build());
    }

    private void handleConnectionRequest(final ConnectionRequest req) throws Exception {
        nativeChannel.config().setAutoRead(false); // disable AutoRead until connection is ready

        Bootstrap b = new Bootstrap();
        b.group(nativeChannel.eventLoop()) // use the same EventLoop
                .channel(nativeChannel.getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new LoggingHandler(LogLevel.DEBUG),
                                appCtx.getBean(ClientDaemonTunnelDecoder.class),
                                appCtx.getBean(ClientDaemonTunnelEncoder.class),
                                appCtx.getBean(ClientDaemonTunnelHandler.class, nativeChannel)
                        );
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = b.connect(serverDaemonHost, serverDaemonPort);
        tunnelChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                tunnelChannel.writeAndFlush(TunnelConnectionRequest.builder().body(req).build());
                nativeChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
            } else {
                logger.error("shit happens", future.cause());
                nativeChannel.writeAndFlush(
                        ConnectionResponse.builder()
                                .ver(SocksConst.VERSION)
                                .rep(ConnectionRep.HOST_UNREACHABLE)
                                .bnd(req.dst())
                                .build())
                        .addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    private void handleDataTransport(DataTransport in) {
        tunnelChannel.writeAndFlush(TunnelDataTransport.builder().body(in).build());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Utils.flushAndClose(tunnelChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("shit happens", e);
        Utils.flushAndClose(nativeChannel);
    }
}
