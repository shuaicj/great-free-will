package shuaicj.hobby.great.free.will.socks.message;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import shuaicj.hobby.great.free.will.socks.SocksDecoder;
import shuaicj.hobby.great.free.will.socks.SocksEncoder;
import shuaicj.hobby.great.free.will.socks.SocksMessage;

/**
 /**
 * SOCKS5 data transport. Just save the ByteBuf inside.
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 *
 * @author shuaicj 2017/10/09
 */
@Getter
@ToString
public class DataTransport implements SocksMessage {

    private final ByteBuf data;

    @Builder
    private DataTransport(ByteBuf data) {
        this.data = data;
    }

    /**
     * Decoder of {@link DataTransport}.
     *
     * @author shuaicj 2017/10/09
     */
    @Component
    public static class Decoder implements SocksDecoder<DataTransport> {

        @Override
        public DataTransport decode(ByteBuf in) throws DecoderException {
            return DataTransport.builder()
                    .data(in.readBytes(in.readableBytes()))
                    .build();
        }
    }

    /**
     * Encoder of {@link DataTransport}.
     *
     * @author shuaicj 2017/10/09
     */
    @Component
    public static class Encoder implements SocksEncoder<DataTransport> {

        @Override
        public void encode(DataTransport msg, ByteBuf out) throws EncoderException {
            out.writeBytes(msg.data);
        }
    }
}
