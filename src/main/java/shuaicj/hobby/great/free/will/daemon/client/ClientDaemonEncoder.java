package shuaicj.hobby.great.free.will.daemon.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.socks.SocksMessage;

/**
 * Netty encoder of client daemon.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonEncoder extends MessageToByteEncoder<SocksMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SocksMessage msg, ByteBuf out) throws Exception {

    }
}
