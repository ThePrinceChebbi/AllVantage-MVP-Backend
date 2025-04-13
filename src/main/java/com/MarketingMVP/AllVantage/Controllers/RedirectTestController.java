package com.MarketingMVP.AllVantage.Controllers;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Services.Accounts.Meta.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.Accounts.Meta.MetaAuth.MetaAuthService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
