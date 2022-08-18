package xyz.idaoteng.auth.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {
    private static final char[] CHARS = "abcdefghijklmnopgrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%&*()_+/-=[]{};:<>?.0123456789".toCharArray();
    private static final char[] NUMS = "0123456789".toCharArray();
    private static final int BOUND_OF_CHARS = CHARS.length;
    private static final int BOUND_OF_NUMS = NUMS.length;

    private static final SecureRandom SECURE_RANDOM;

    static {
        try {
            SECURE_RANDOM = SecureRandom.getInstanceStrong();
            //SECURE_RANDOM = new SecureRandom();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("PasswordUtil工具类: 算法名有误！？");
        }
    }

    //对密码进行sha256加密
    public static String encrypt(String originalPassword) {
        return DigestUtils.sha256Hex(originalPassword);
    }

    //加盐加密
    public static String encrypt(String originalPassword, String salt) {
        return encrypt(originalPassword + salt);
    }

    private static String getString(int length, char[] chars, int bound) {
        char[] randomString = new char[length];

        for (int i = 0; i < length; i++) {
            randomString[i] = chars[SECURE_RANDOM.nextInt(bound)];
        }
        return new String(randomString);
    }

    //随机字符串
    public static String randomString(int length) {
        return getString(length, CHARS, BOUND_OF_CHARS);
    }

    //随机纯数字字符串
    public static String randomNumericalString(int length) {
        return getString(length, NUMS, BOUND_OF_NUMS);
    }
}
