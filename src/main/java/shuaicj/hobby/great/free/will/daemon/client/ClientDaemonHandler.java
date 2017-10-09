package shuaicj.hobby.great.free.will.daemon.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.socks.SocksConst;
import shuaicj.hobby.great.free.will.socks.message.AuthMethodRequest;
import shuaicj.hobby.great.free.will.socks.message.AuthMethodResponse;
import shuaicj.hobby.great.free.will.socks.message.ConnectionRequest;
import shuaicj.hobby.great.free.will.socks.type.AuthMethod;
import shuaicj.hobby.great.free.will.util.Utils;

/**
 * Netty handler of client daemon.
 *
 * @author shuaicj 2017/10/09
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;
    private Channel remoteChannel;

    @Autowired private ApplicationContext appCtx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannel = ctx.channel();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof AuthMethodRequest) {
            handleAuthMethodRequest(ctx, (AuthMethodRequest) msg);
            return;
        }
        if (msg instanceof ConnectionRequest) {
            handleConnectionRequest(ctx, (ConnectionRequest) msg);
            return;
        }
        if (msg instanceof ByteBuf) {
            handleDataTransport(ctx, (ByteBuf) msg);
            return;
        }
    }

    private void handleAuthMethodRequest(ChannelHandlerContext ctx, AuthMethodRequest req) {
        if (!req.methods().contains(AuthMethod.NO_AUTHENTICATION_REQUIRED)) {
            ctx.close(); // only support NO_AUTHENTICATION_REQUIRED
            return;
        }
        AuthMethodResponse rsp = AuthMethodResponse.builder()
                .ver(SocksConst.VERSION)
                .method(AuthMethod.NO_AUTHENTICATION_REQUIRED)
                .build();
        ctx.writeAndFlush(rsp);
    }

    private void handleConnectionRequest(ChannelHandlerContext ctx, ConnectionRequest req) {

    }

    private void handleDataTransport(ChannelHandlerContext ctx, ByteBuf in) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Utils.flushAndClose(remoteChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("shit happens", e);
        Utils.flushAndClose(clientChannel);
    }
}
