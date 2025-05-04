package com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.LinkedIn.LinkedinToken;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class LinkedinTokenDTOMapper implements Function<LinkedinToken, LinkedinTokenDTO> {
    private final AESEncryptionService aesEncryptionService;

    public LinkedinTokenDTOMapper(AESEncryptionService aesEncryptionService) {
        this.aesEncryptionService = aesEncryptionService;
    }

    @Override
    public LinkedinTokenDTO apply(LinkedinToken linkedinToken) {
        return new LinkedinTokenDTO(
                linkedinToken.getAccount().getLinkedinId(),
                aesEncryptionService.decrypt(linkedinToken.getAccessToken()),
                linkedinToken.getExpiresIn(),
                TimeUnit.SECONDS
        );
    }
}
