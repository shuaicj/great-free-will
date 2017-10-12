package shuaicj.hobby.great.free.will.daemon.client;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelConnectionResponse;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelDataTransport;

/**
 * Netty decoder of client daemon for tunnel.
 *
 * @author shuaicj 2017/10/12
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonTunnelDecoder extends ByteToMessageDecoder {

    private State state = State.INIT;

    @Autowired private TunnelConnectionResponse.Decoder tunnelConnectionResponseDecoder;
    @Autowired private TunnelDataTransport.Decoder tunnelDataTransportDecoder;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state) {
            case INIT: {
                TunnelConnectionResponse msg = tunnelConnectionResponseDecoder.decode(in);
                if (msg != null) {
                    logger.info("receive {}", msg);
                    out.add(msg);
                    state = State.TUNNEL_CONNECTION_RESPONSE_DECODED;
                }
                break;
            }
            case TUNNEL_CONNECTION_RESPONSE_DECODED: {
                TunnelDataTransport msg = tunnelDataTransportDecoder.decode(in);
                if (msg != null) {
                    logger.info("receive {}", msg);
                    out.add(msg);
                }
                break;
            }
        }
    }

    private enum State {
        INIT,
        TUNNEL_CONNECTION_RESPONSE_DECODED
    }
}
