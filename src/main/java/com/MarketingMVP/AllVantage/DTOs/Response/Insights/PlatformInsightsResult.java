package com.MarketingMVP.AllVantage.DTOs.Response.Insights;

import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import lombok.Getter;

@Getter
public class PlatformInsightsResult {
    // Getters
    private final boolean success;
    private final PlatformType platform;
    private final String message;
    private final Object data;

    public PlatformInsightsResult(boolean success, PlatformType platform, String message, Object data) {
        this.success = success;
        this.platform = platform;
        this.message = message;
        this.data = data;
    }

    public static PlatformInsightsResult success(PlatformType platform, Object responseData) {

        return new PlatformInsightsResult(true, platform, "Insights fetched successfully", responseData);
    }

    public static PlatformInsightsResult failure(PlatformType platform, String message) {
        return new PlatformInsightsResult(false, platform, message, null);
    }

}
