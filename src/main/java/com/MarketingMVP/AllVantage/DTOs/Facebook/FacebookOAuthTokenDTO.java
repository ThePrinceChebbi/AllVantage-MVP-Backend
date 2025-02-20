package com.MarketingMVP.AllVantage.DTOs.Facebook;

import java.util.concurrent.TimeUnit;

public record FacebookOAuthTokenDTO(
        String accessToken,
        int expiresIn,
        TimeUnit expiresInTimeUnit
) {
}
