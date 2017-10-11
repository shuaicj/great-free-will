package shuaicj.hobby.great.free.will.protocol.tunnel.message;

import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.BODY_LEN_LEN;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.protocol.Message;
import shuaicj.hobby.great.free.will.protocol.MessageEncoder;
import shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionResponse;

/**
 * Tunnel connection response. It consists of a 4 byte body length value
 * and a socks {@link shuaicj.hobby.great.free.will.protocol.socks.message.ConnectionResponse}.
 *
 *   +-------------+--------------------+
 *   | BODY_LENGTH |        BODY        |
 *   +-------------+--------------------+
 *   |      4      | ConnectionResponse |
 *   +-------------+--------------------+
 *
 * @author shuaicj 2017/10/11
 */
@Getter
@ToString
public class TunnelConnectionResponse implements Message {

    private final ConnectionResponse body;

    @Builder
    private TunnelConnectionResponse(ConnectionResponse body) {
        this.body = body;
    }

    @Override
    public int length() {
        return BODY_LEN_LEN + body.length();
    }

    /**
     * Encoder of {@link TunnelConnectionResponse}.
     *
     * @author shuaicj 2017/09/27
     */
    @Component
    public static class Encoder implements MessageEncoder<TunnelConnectionResponse> {

        @Autowired ConnectionResponse.Encoder bodyEncoder;

        @Override
        public void encode(TunnelConnectionResponse msg, ByteBuf out) throws EncoderException {
            out.writeInt(msg.body.length());
            bodyEncoder.encode(msg.body, out);
        }
    }
}
