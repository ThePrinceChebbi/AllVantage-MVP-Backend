package com.MarketingMVP.AllVantage.DTOs.Post;

import lombok.Builder;

@Builder
public record SmallPostDTO(
    Long id,
    String title,
    String thumbnailUrl,
    String creatorUsername,
    String date
) {
}
