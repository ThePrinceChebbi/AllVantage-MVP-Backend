package com.MarketingMVP.AllVantage.DTOs.LinkedIn.Account;

import java.util.Date;

public record LinkedInAccountDTO(
        Long id,
        String accountName,
        Date createdTime,
        Date updatedTime
) {
}
