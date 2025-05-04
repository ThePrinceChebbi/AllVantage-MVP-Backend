package com.MarketingMVP.AllVantage.Controllers;

import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Facebook.FacebookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
public class RedirectTestController {
    private final FacebookService facebookService;

    public RedirectTestController(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @GetMapping("/")
    public String redirect() {
        return "Redirected Successfully";
    }

    @Value("${encryptionKey}")
    private String envValue;

    @GetMapping("/test-env")
    public String test() {
        return envValue;
    }

    @GetMapping("/{accountId}/test-refresh")
    public ResponseEntity<Object> testRefresh(@PathVariable Long accountId) {
        return facebookService.testRefreshMethod(accountId);
    }

}
