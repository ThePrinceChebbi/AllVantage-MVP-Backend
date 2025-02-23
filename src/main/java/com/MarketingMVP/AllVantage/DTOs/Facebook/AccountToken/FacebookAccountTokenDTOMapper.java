package com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class FacebookAccountTokenDTOMapper implements Function<FacebookAccountToken, FacebookAccountTokenDTO> {
    private final AESEncryptionService aesEncryptionService;

    public FacebookAccountTokenDTOMapper(AESEncryptionService aesEncryptionService) {
        this.aesEncryptionService = aesEncryptionService;
    }

    @Override
    public FacebookAccountTokenDTO apply(FacebookAccountToken facebookOAuthToken) {
        return new FacebookAccountTokenDTO(
                facebookOAuthToken.getAccount().getFacebookId(),
                aesEncryptionService.decrypt(facebookOAuthToken.getAccessToken()),
                facebookOAuthToken.getExpiresIn(),
                TimeUnit.SECONDS
        );
    }
}
