package com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken;

import java.util.concurrent.TimeUnit;

public record LinkedinTokenDTO(
        String linkedinId,
        String accessToken,
        int expiresIn,
        TimeUnit expiresInTimeUnit
) {
}
