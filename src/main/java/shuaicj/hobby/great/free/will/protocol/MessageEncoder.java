package shuaicj.hobby.great.free.will.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;

/**
 * Message encoder interface.
 *
 * @author shuaicj 2017/09/27
 */
public interface MessageEncoder<T extends Message> {

    /**
     * @param msg message
     * @param out where data write to
     * @throws EncoderException if error occurs
     */
    void encode(T msg, ByteBuf out) throws EncoderException;
}
