package shuaicj.hobby.great.free.will.daemon.server;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.tunnel.message.TunnelDataTransport;

/**
 * Netty decoder of server daemon for tunnel.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Scope("prototype")
@Profile("server")
@Slf4j
public class ServerDaemonTunnelDecoder extends ByteToMessageDecoder {

    private State state = State.INIT;

    @Autowired private TunnelConnectionRequest.Decoder tunnelConnectionRequestDecoder;
    @Autowired private TunnelDataTransport.Decoder tunnelDataTransportDecoder;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state) {
            case INIT: {
                TunnelConnectionRequest msg = tunnelConnectionRequestDecoder.decode(in);
                if (msg != null) {
                    logger.info("receive {}", msg);
                    out.add(msg);
                    state = State.TUNNEL_CONNECTION_REQUEST_DECODED;
                }
                break;
            }
            case TUNNEL_CONNECTION_REQUEST_DECODED: {
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
        TUNNEL_CONNECTION_REQUEST_DECODED
    }
}
