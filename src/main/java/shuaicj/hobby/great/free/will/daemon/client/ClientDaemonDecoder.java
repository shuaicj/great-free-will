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
import shuaicj.hobby.great.free.will.socks.SocksState;
import shuaicj.hobby.great.free.will.socks.message.AuthMethodRequest;
import shuaicj.hobby.great.free.will.socks.message.ConnectionRequest;

/**
 * Netty decoder of client daemon.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Scope("prototype")
@Profile("client")
@Slf4j
public class ClientDaemonDecoder extends ByteToMessageDecoder {

    private SocksState state = SocksState.INIT;

    @Autowired private AuthMethodRequest.Decoder authMethodRequestDecoder;
    @Autowired private ConnectionRequest.Decoder connectionRequestDecoder;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state) {
            case INIT: {
                AuthMethodRequest req = authMethodRequestDecoder.decode(in);
                if (req != null) {
                    logger.info("AuthMethodRequest {}", req);
                    out.add(req);
                    state = SocksState.AUTH_METHOD_OK;
                }
                break;
            }
            case AUTH_METHOD_OK: {
                ConnectionRequest req = connectionRequestDecoder.decode(in);
                if (req != null) {
                    logger.info("ConnectionRequest {}", req);
                    out.add(req);
                    state = SocksState.CONNECTION_OK;
                }
                break;
            }
            case CONNECTION_OK: {
                logger.info("Data transport...");
                out.add(in.readBytes(in.readableBytes()));
                break;
            }
        }
    }
}
