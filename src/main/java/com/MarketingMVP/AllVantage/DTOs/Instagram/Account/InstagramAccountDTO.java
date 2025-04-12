package com.MarketingMVP.AllVantage.DTOs.Instagram.Account;

import java.util.Date;

public record InstagramAccountDTO(
        Long id,
        String accountName,
        Date createdTime,
        Date updatedTime
) {
}
