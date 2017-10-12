package shuaicj.hobby.great.free.will.daemon.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.socks.message.AuthMethodResponse;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionResponse;
import shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport;

/**
 * Netty encoder of client daemon for native.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonNativeEncoder extends MessageToByteEncoder<Message> {

    @Autowired private AuthMethodResponse.Encoder authMethodResponseEncoder;
    @Autowired private ConnectionResponse.Encoder connectionResponseEncoder;
    @Autowired private DataTransport.Encoder dataTransportEncoder;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        if (msg instanceof AuthMethodResponse) {
            logger.info("send {}", msg);
            authMethodResponseEncoder.encode((AuthMethodResponse) msg, out);
            return;
        }
        if (msg instanceof ConnectionResponse) {
            logger.info("send {}", msg);
            connectionResponseEncoder.encode((ConnectionResponse) msg, out);
            return;
        }
        if (msg instanceof DataTransport) {
            logger.info("send {}", msg);
            dataTransportEncoder.encode((DataTransport) msg, out);
            return;
        }
        throw new EncoderException("unsupported message type " + msg);
    }
}
