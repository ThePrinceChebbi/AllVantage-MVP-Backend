package com.MarketingMVP.AllVantage.DTOs.Facebook.Account;

import java.util.Date;

public record FacebookAccountDTO(
        Long id,
        String accountName,
        Date createdTime,
        Date updatedTime
) {
}
