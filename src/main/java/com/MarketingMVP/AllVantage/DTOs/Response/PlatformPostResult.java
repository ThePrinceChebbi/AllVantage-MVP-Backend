package com.MarketingMVP.AllVantage.DTOs.Response;

import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import lombok.Getter;

@Getter
public class PlatformPostResult {
    private final PlatformType platform;
    private final boolean success;
    private final Object result;

    public PlatformPostResult(PlatformType platform, boolean success, Object result) {
        this.platform = platform;
        this.success = success;
        this.result = result;
    }

    public static PlatformPostResult success(PlatformType platform, Object result) {
        return new PlatformPostResult(platform, true, result);
    }

    public static PlatformPostResult failure(PlatformType platform, String errorMessage) {
        return new PlatformPostResult(platform, false, errorMessage);
    }

}
