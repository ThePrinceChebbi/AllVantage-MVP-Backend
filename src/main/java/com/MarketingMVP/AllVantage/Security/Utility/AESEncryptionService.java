package com.MarketingMVP.AllVantage.Security.Utility;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class AESEncryptionService {

    private final String secretKey;

    public AESEncryptionService(@Value("${encryptionKey}") String secretKey) {
        this.secretKey = secretKey;
    }

    // This method is used to generate a secret key for AES encryption by hashing the key
    // stored in the application.properties file and returning a 32 byte long key
    private SecretKey getAESKey(String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);
        return new SecretKeySpec(Arrays.copyOf(keyBytes, 32), "AES");  // 256-bit key
    }

    public String encrypt(String value) throws Exception {
        SecretKey key = getAESKey(this.secretKey);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
    }

    public String decrypt(String encryptedValue) {
        try{
            SecretKey key = getAESKey(this.secretKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue)));

        }catch (Exception e){
            return null;
        }
    }
}


