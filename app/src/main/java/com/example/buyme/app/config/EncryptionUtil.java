package com.example.buyme.app.config;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = System.getenv("ENCRYPTION_SECRET_KEY"); // 환경변수에서 비밀 키를 가져옴

    public static String encrypt(String value) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("데이터 암호화 중 오류가 발생했습니다.", e);
        }
    }

    public static String decrypt(String encrypted) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception e) {
            throw new RuntimeException("데이터 복호화 중 오류가 발생하였습니다.", e);
        }
    }
}