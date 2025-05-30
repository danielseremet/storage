package s.service;


import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.util.Base64;

public class AESutil {

    private static final String ALGORITHM = "AES";

    public static String encrypt(String content, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,key);
        byte[] encrypt = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encrypt);
    }

    public static String decrypt(String content, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,key);
        byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(content));
        byte[] original = cipher.doFinal(decrypt);
        return new String(original);
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}
