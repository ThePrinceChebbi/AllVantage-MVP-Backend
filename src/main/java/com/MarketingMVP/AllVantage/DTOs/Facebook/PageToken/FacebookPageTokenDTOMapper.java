package com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class FacebookPageTokenDTOMapper implements Function<FacebookPageToken, FacebookPageTokenDTO> {
    private final AESEncryptionService aesEncryptionService;

    public FacebookPageTokenDTOMapper(AESEncryptionService aesEncryptionService) {
        this.aesEncryptionService = aesEncryptionService;
    }

    @Override
    public FacebookPageTokenDTO apply(FacebookPageToken facebookPageToken) {
        return new FacebookPageTokenDTO(
                facebookPageToken.getPage().getFacebookPageId(),
                aesEncryptionService.decrypt(facebookPageToken.getAccessToken()),
                facebookPageToken.getExpiresIn(),
                TimeUnit.SECONDS
        );
    }
}
