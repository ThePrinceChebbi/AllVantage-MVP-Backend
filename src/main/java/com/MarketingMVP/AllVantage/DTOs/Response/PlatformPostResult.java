package com.MarketingMVP.AllVantage.DTOs.Response;

import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import lombok.Getter;

@Getter
public class PlatformPostResult {
    private final PlatformType platform;
    private final boolean success;
    private final String message;

    public PlatformPostResult(PlatformType platform, boolean success, String message) {
        this.platform = platform;
        this.success = success;
        this.message = message;
    }

    public static PlatformPostResult success(PlatformType platform, String message) {
        return new PlatformPostResult(platform, true, message);
    }

    public static PlatformPostResult failure(PlatformType platform, String errorMessage) {
        return new PlatformPostResult(platform, false, errorMessage);
    }

}
