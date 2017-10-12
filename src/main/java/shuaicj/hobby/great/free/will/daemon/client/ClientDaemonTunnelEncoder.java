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
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelDataTransport;

/**
 * Netty encoder of client daemon for tunnel.
 *
 * @author shuaicj 2017/10/12
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonTunnelEncoder extends MessageToByteEncoder<Message> {

    @Autowired private TunnelConnectionRequest.Encoder tunnelConnectionRequestEncoder;
    @Autowired private TunnelDataTransport.Encoder tunnelDataTransportEncoder;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        if (msg instanceof TunnelConnectionRequest) {
            logger.info("send {}", msg);
            tunnelConnectionRequestEncoder.encode((TunnelConnectionRequest) msg, out);
            return;
        }
        if (msg instanceof TunnelDataTransport) {
            logger.info("send {}", msg);
            tunnelDataTransportEncoder.encode((TunnelDataTransport) msg, out);
            return;
        }
        throw new EncoderException("unsupported message type " + msg);
    }
}
