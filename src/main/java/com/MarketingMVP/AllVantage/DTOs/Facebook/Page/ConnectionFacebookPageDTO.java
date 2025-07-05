package com.MarketingMVP.AllVantage.DTOs.Facebook.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionFacebookPageDTO {
    private String id;
    private String name;
    private Long accountId;
    private String imageUrl;
}
