package shuaicj.hobby.great.free.will.daemon.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.socks.SocksMessage;
import shuaicj.hobby.great.free.will.socks.message.AuthMethodResponse;
import shuaicj.hobby.great.free.will.socks.message.ConnectionResponse;
import shuaicj.hobby.great.free.will.socks.message.DataTransport;

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

    @Autowired private AuthMethodResponse.Encoder authMethodResponseEncoder;
    @Autowired private ConnectionResponse.Encoder connectionResponseEncoder;
    @Autowired private DataTransport.Encoder dataTransportEncoder;

    @Override
    protected void encode(ChannelHandlerContext ctx, SocksMessage msg, ByteBuf out) throws Exception {
        if (msg instanceof AuthMethodResponse) {
            authMethodResponseEncoder.encode((AuthMethodResponse) msg, out);
            return;
        }
        if (msg instanceof ConnectionResponse) {
            connectionResponseEncoder.encode((ConnectionResponse) msg, out);
            return;
        }
        if (msg instanceof DataTransport) {
            dataTransportEncoder.encode((DataTransport) msg, out);
        }
    }
}
