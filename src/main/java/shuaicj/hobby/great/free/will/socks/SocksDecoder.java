package shuaicj.hobby.great.free.will.socks;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

/**
 * Socks message decoder interface.
 *
 * @author shuaicj 2017/09/27
 */
public interface SocksDecoder<T> {

    /**
     * @param in where data read from
     * @return socks message, or null if need more data
     * @throws DecoderException if error occurs
     */
    T decode(ByteBuf in) throws DecoderException;
}
