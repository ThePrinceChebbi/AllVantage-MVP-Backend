package com.MarketingMVP.AllVantage.DTOs.Instagram;


import java.util.Date;

public record InstagramAccountDTO(
        Long id,
        String instagramId,
        String accountName,
        Date createdTime,
        Date updatedTime,
        Long facebookPageId
) {
}
