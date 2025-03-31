package com.MarketingMVP.AllVantage.DTOs.Response.Insights;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class InsightData {
    // Getters
    private String name;
    private String title;
    private String description;
    private List<Map<String, Object>> values; // Contains value and end_time

    public InsightData(String name, String title, String description, List<Map<String, Object>> values) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.values = values;
    }

}
