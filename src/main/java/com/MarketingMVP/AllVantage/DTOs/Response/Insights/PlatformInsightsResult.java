package com.MarketingMVP.AllVantage.DTOs.Response.Insights;

import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class PlatformInsightsResult {
    // Getters
    private final boolean success;
    private final PlatformType platform;
    private final String message;
    private final List<InsightData> insights;

    public PlatformInsightsResult(boolean success, PlatformType platform, String message, List<InsightData> insights) {
        this.success = success;
        this.platform = platform;
        this.message = message;
        this.insights = insights;
    }

    public static PlatformInsightsResult success(PlatformType platform, Map<String, Object> responseData) {
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseData.get("data");

        List<InsightData> insights = dataList.stream().map(data -> new InsightData(
                (String) data.get("name"),
                (String) data.get("title"),
                (String) data.get("description"),
                (List<Map<String, Object>>) data.get("values")
        )).toList();

        return new PlatformInsightsResult(true, platform, "Insights fetched successfully", insights);
    }

    public static PlatformInsightsResult failure(PlatformType platform, String message) {
        return new PlatformInsightsResult(false, platform, message, null);
    }

}
