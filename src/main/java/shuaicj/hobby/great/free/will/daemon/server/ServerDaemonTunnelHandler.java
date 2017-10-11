package shuaicj.hobby.great.free.will.daemon.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionAddrType;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionCmd;
import shuaicj.hobby.great.free.will.protocol.socks.type.ConnectionRep;
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

    private Channel nativeChannel;
    private Channel tunnelChannel;

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
        throw new IllegalStateException("illegal message");
    }

    private void handleAuthMethodRequest(AuthMethodRequest req) {
        if (!req.methods().contains(AuthMethod.NO_AUTHENTICATION_REQUIRED)) {
            nativeChannel.close(); // only support NO_AUTHENTICATION_REQUIRED
            return;
        }
        AuthMethodResponse rsp = AuthMethodResponse.builder()
                .ver(SocksConst.VERSION)
                .method(AuthMethod.NO_AUTHENTICATION_REQUIRED)
                .build();
        nativeChannel.writeAndFlush(rsp);
    }

    private void handleConnectionRequest(final ConnectionRequest req) throws Exception {
        if (!req.cmd().equals(ConnectionCmd.CONNECT)) {
            nativeChannel.close(); // only support TCP CONNECT
            return;
        }

        nativeChannel.config().setAutoRead(false); // disable AutoRead until connection is ready

        Bootstrap b = new Bootstrap();
        b.group(nativeChannel.eventLoop()) // use the same EventLoop
                .channel(nativeChannel.getClass())
                .handler(appCtx.getBean(shuaicj.hobby.great.free.will.daemon.client.ClientDaemonTunnelHandler.class, nativeChannel))
                .option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = b.connect(inetAddress(req), req.dst().port());
        tunnelChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                nativeChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
                ConnectionResponse rsp = ConnectionResponse.builder()
                        .ver(SocksConst.VERSION)
                        .rep(ConnectionRep.SUCCEEDED)
                        .bnd(req.dst())
                        .build();
                nativeChannel.writeAndFlush(rsp);
            } else {
                nativeChannel.close();
            }
        });
    }

    private void handleDataTransport(DataTransport in) {
        tunnelChannel.writeAndFlush(in.data());
    }

    private InetAddress inetAddress(ConnectionRequest req) throws UnknownHostException {
        if (req.dst().type().equals(ConnectionAddrType.DOMAIN_NAME)) {
            return InetAddress.getByName(new String(req.dst().addr())); // domain name
        }
        return InetAddress.getByAddress(req.dst().addr()); // IPv4, IPv6
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
