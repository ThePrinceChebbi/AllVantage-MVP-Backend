package com.MarketingMVP.AllVantage.DTOs.Facebook.OAuthToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class FacebookOAuthTokenDTOMapper implements Function<FacebookOAuthToken, FacebookOAuthTokenDTO> {
    private final AESEncryptionService aesEncryptionService;

    public FacebookOAuthTokenDTOMapper(AESEncryptionService aesEncryptionService) {
        this.aesEncryptionService = aesEncryptionService;
    }

    @Override
    public FacebookOAuthTokenDTO apply(FacebookOAuthToken facebookOAuthToken) {
        return new FacebookOAuthTokenDTO(
                aesEncryptionService.decrypt(facebookOAuthToken.getAccessToken()),
                facebookOAuthToken.getExpiresIn(),
                TimeUnit.SECONDS
        );
    }
}
