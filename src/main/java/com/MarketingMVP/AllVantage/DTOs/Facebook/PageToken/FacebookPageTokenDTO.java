package com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken;

import java.util.concurrent.TimeUnit;

public record FacebookPageTokenDTO(
        String facebookPageId,
        String accessToken,
        int expiresIn,
        TimeUnit expiresInTimeUnit
) {
}
