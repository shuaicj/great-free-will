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
import shuaicj.hobby.great.free.will.protocol.socks.message.AuthMethodRequest;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionRequest;
import shuaicj.hobby.great.free.will.protocol.socks.message.DataTransport;

/**
 * Netty decoder of client daemon for native.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonNativeDecoder extends ByteToMessageDecoder {

    private State state = State.INIT;

    @Autowired private AuthMethodRequest.Decoder authMethodRequestDecoder;
    @Autowired private ConnectionRequest.Decoder connectionRequestDecoder;
    @Autowired private DataTransport.Decoder dataTransportDecoder;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state) {
            case INIT: {
                AuthMethodRequest msg = authMethodRequestDecoder.decode(in);
                if (msg != null) {
                    logger.info("receive {}", msg);
                    out.add(msg);
                    state = State.AUTH_METHOD_REQUEST_DECODED;
                }
                break;
            }
            case AUTH_METHOD_REQUEST_DECODED: {
                ConnectionRequest msg = connectionRequestDecoder.decode(in);
                if (msg != null) {
                    logger.info("receive {}", msg);
                    out.add(msg);
                    state = State.CONNECTION_REQUEST_DECODED;
                }
                break;
            }
            case CONNECTION_REQUEST_DECODED: {
                DataTransport msg = dataTransportDecoder.decode(in);
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
        AUTH_METHOD_REQUEST_DECODED,
        CONNECTION_REQUEST_DECODED
    }
}
