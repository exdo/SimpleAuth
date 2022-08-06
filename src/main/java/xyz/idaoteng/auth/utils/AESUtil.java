package xyz.idaoteng.auth.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
    private static final String KEY_TEXT = "WhpWSLq3*@J!R5";
    private static final SecretKey KEY;

    static {
        byte[] keyBytes = DigestUtils.md5(KEY_TEXT);
        //创建密钥，128bits（经过MD5散列后得到的byte数组大小必然是16）
        //AES 算法的 key只能是128bits或196bits或256bits，对应的字节大小为16、24、32字节
        KEY = new  SecretKeySpec(keyBytes, "AES");
    }

    //加密
    public static String encrypt(String originalText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, KEY);
            byte[] cipherText = cipher.doFinal(originalText.getBytes());
            return Hex.encodeHexString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("加密异常");
        }
    }

    //解密
    public static String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, KEY);
            byte[] plainText = cipher.doFinal(Hex.decodeHex(cipherText));
            return new String(plainText);
        } catch (Exception e) {
            return null;
        }
    }
}
