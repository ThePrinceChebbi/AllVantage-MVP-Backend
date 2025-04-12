package com.MarketingMVP.AllVantage.DTOs.Instagram.AccountToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Instagram.InstagramToken;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class InstagramTokenDTOMapper implements Function<InstagramToken, InstagramTokenDTO> {
    private final AESEncryptionService aesEncryptionService;

    public InstagramTokenDTOMapper(AESEncryptionService aesEncryptionService) {
        this.aesEncryptionService = aesEncryptionService;
    }

    @Override
    public InstagramTokenDTO apply(InstagramToken instagramToken) {
        return new InstagramTokenDTO(
                instagramToken.getAccount().getInstagramId(),
                aesEncryptionService.decrypt(instagramToken.getAccessToken()),
                instagramToken.getExpiresIn(),
                TimeUnit.SECONDS
        );
    }
}
