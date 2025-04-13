package com.MarketingMVP.AllVantage.DTOs.Instagram.Account;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;

import java.util.Date;

public record InstagramAccountDTO(
        Long id,
        String accountName,
        Date createdTime,
        Date updatedTime,
        FacebookPage facebookPage
) {
}
