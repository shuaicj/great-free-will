package shuaicj.hobby.great.free.will.protocol.tunnel.cipher;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
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

    private static final String RAND = "aevtfd23!#$#";

    private final String secret;
    private final Cipher encrypter;

    private final Cipher decrypter;

    public TunnelCipher(@Value("${daemon.secret}") String secret) throws GeneralSecurityException {
        this.secret = secret;
        this.encrypter = Cipher.getInstance("AES/CFB16/NoPadding");
        this.decrypter = Cipher.getInstance("AES/CFB16/NoPadding");
        byte[] key = DigestUtils.md5Digest(secret.getBytes());
        byte[] iv = DigestUtils.md5Digest((secret + RAND).getBytes());
        encrypter.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        decrypter.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
    }

    public void resetEncrypter(int salt) {
        byte[] key = DigestUtils.md5Digest((secret + salt).getBytes());
        byte[] iv = DigestUtils.md5Digest((secret + salt + RAND).getBytes());
        try {
            encrypter.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        } catch (GeneralSecurityException e) {
            logger.error("shit happens", e);
        }
    }

    public void resetDecrypter(int salt) throws GeneralSecurityException {
        byte[] key = DigestUtils.md5Digest((secret + salt).getBytes());
        byte[] iv = DigestUtils.md5Digest((secret + salt + RAND).getBytes());
        try {
            decrypter.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        } catch (GeneralSecurityException e) {
            logger.error("shit happens", e);
        }
    }

    public Cipher encrypter() {
        return encrypter;
    }

    public Cipher decrypter() {
        return decrypter;
    }
}
