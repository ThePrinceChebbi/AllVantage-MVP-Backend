package com.MarketingMVP.AllVantage.DTOs.Instagram;


import java.util.Date;

public record InstagramAccountDTO(
        Long id,
        String accountName,
        Date createdTime,
        String instagramUsername,
        Long facebookPageId
) {
}
