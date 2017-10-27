package shuaicj.hobby.great.free.will.protocol.tunnel.cipher;

import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.BODY_LEN_LEN;
import static shuaicj.hobby.great.free.will.protocol.tunnel.TunnelConst.SALT_LEN;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * Encrypt and decrypt tunnel data.
 *
 * @author shuaicj 2017/10/20
 */
@Component
@Scope("prototype")
@Slf4j
public class TunnelCipher {

    private byte[] buf;

    private Cipher encrypter;
    private Cipher decrypter;

    @Autowired private TunnelCipherKeyIV kv;

    private void initBuf() {
        if (buf == null) {
            buf = new byte[2048];
        }
    }

    private void initEncrypter() throws GeneralSecurityException {
        if (encrypter == null) {
            encrypter = Cipher.getInstance("AES/CFB32/NoPadding");
            encrypter.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kv.key, "AES"), new IvParameterSpec(kv.iv));
        }
    }

    private void initDecrypter() throws GeneralSecurityException {
        if (decrypter == null) {
            decrypter = Cipher.getInstance("AES/CFB32/NoPadding");
            decrypter.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kv.key, "AES"), new IvParameterSpec(kv.iv));
        }
    }

    public ByteBuf encrypt(ByteBuf in) throws GeneralSecurityException {
        initBuf();
        initEncrypter();
        return doCrypto(encrypter, in);
    }

    public ByteBuf decrypt(ByteBuf in) throws GeneralSecurityException {
        initBuf();
        initDecrypter();
        return doCrypto(decrypter, in);
    }

    private ByteBuf doCrypto(Cipher cipher, ByteBuf in) throws GeneralSecurityException {
        if (!in.isReadable(SALT_LEN + BODY_LEN_LEN)) {
            return null;
        }
        int mark = in.readerIndex();

        in.readBytes(buf, 0, SALT_LEN + BODY_LEN_LEN);
        int bodyLength = bodyLength(buf, SALT_LEN);
        if (!in.isReadable(bodyLength)) {
            in.readerIndex(mark);
            cipher.doFinal();
            return null;
        }

        ByteBuf out = in.alloc().buffer(SALT_LEN + BODY_LEN_LEN + bodyLength);
        out.writeBytes(buf, 0, SALT_LEN + BODY_LEN_LEN);

        for (int remain = bodyLength; remain > 0; remain -= buf.length) {
            if (remain > buf.length) {
                in.readBytes(buf);
                cipher.update(buf, 0, buf.length, buf);
                out.writeBytes(buf);
            } else {
                in.readBytes(buf, 0, remain);
                cipher.doFinal(buf, 0, remain, buf);
                out.writeBytes(buf, 0, remain);
            }
        }

        return out;
    }

    private int bodyLength(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    @Component
    static class TunnelCipherKeyIV {

        byte[] key, iv;

        TunnelCipherKeyIV(@Value("${daemon.secret}") String secret) {
            key = DigestUtils.md5Digest(secret.getBytes());
            iv = DigestUtils.md5Digest((secret + "aevtfd23!#$#").getBytes());
        }
    }
}
