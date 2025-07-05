package com.MarketingMVP.AllVantage.DTOs.MetaInsights;

public record MetaInsights(
        String postId,
        Integer impressions,
        Integer engagement,
        Integer videoViews,
        Integer postClicks
) {
}
