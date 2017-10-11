package shuaicj.hobby.great.free.will.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

/**
 * Message decoder interface.
 *
 * @author shuaicj 2017/09/27
 */
public interface MessageDecoder<T extends Message> {

    /**
     * @param in where data read from
     * @return a message and readerIndex of 'in' is moved backwards after read;
     *         or null if need more data, and readerIndex must keep unchanged.
     * @throws DecoderException if error occurs, the state of readerIndex is not guaranteed
     */
    T decode(ByteBuf in) throws DecoderException;
}
