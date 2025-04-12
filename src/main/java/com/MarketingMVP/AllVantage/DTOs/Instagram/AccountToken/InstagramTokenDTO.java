package com.MarketingMVP.AllVantage.DTOs.Instagram.AccountToken;

import java.util.concurrent.TimeUnit;

public record InstagramTokenDTO(
        String instagramAccountId,
        String accessToken,
        int expiresIn,
        TimeUnit expiresInTimeUnit
) {
}
