package com.MarketingMVP.AllVantage.DTOs.Facebook.Page;

import java.util.Date;

public record FacebookPageDTO(
        Long id,
        String pageName,
        Date connectedAt,
        String pageUrl
) {
}
