package shuaicj.hobby.great.free.will.socks;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;

/**
 * Socks message encoder interface.
 *
 * @author shuaicj 2017/09/27
 */
public interface SocksEncoder<T extends SocksMessage> {

    /**
     * @param msg socks message
     * @param out where data write to
     * @throws EncoderException if error occurs
     */
    void encode(T msg, ByteBuf out) throws EncoderException;
}
