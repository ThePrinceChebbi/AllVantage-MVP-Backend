package com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken;

import java.util.concurrent.TimeUnit;

public record FacebookAccountTokenDTO(
        String facebookAccountId,
        String accessToken,
        int expiresIn,
        TimeUnit expiresInTimeUnit
) {
}
