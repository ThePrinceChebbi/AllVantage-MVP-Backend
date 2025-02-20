package com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Entity
@Table(name = "facebook_oauth_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FacebookOAuthToken {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "facebook_oauth_token_sequence"
    )
    @SequenceGenerator(
            name = "facebook_oauth_token_sequence",
            sequenceName = "facebook_oauth_token_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private String accessToken;

    @ManyToOne
    private FacebookAccount account;

    @NotNull
    private int expiresIn;

    @NotNull
    private FacebookOAuthTokenType oAuthTokenType;

    // 🔐 Secret Key for AES Encryption (Move this to config/env variables)
    private static final String SECRET_KEY = "YOUR_32_CHAR_SECRET_KEY";

    // 🔐 Encrypt token before saving
    public void setAccessToken(String token) {
        try {
            this.accessToken = encrypt(token);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting access token", e);
        }
    }

    // 🔐 Decrypt token when retrieving
    public String getAccessToken() {
        try {
            return decrypt(this.accessToken);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting access token", e);
        }
    }

    // 🔐 AES Encryption
    private static String encrypt(String value) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
    }

    // 🔐 AES Decryption
    private static String decrypt(String encryptedValue) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue)));
    }
}
