package com.MarketingMVP.AllVantage.Controllers;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Services.Accounts.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/home")
public class RedirectTestController {
    private final FacebookService facebookService;
    private final FileService fileService;

    public RedirectTestController(FacebookService facebookService, FileService fileService) {
        this.facebookService = facebookService;
        this.fileService = fileService;
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

    @PostMapping("/{pageId}/test-facebook")
    public String testFacebookPackage(@RequestParam MultipartFile file,
                                       @PathVariable Long pageId) {
        try {
            FacebookTemplate facebookTemplate = new FacebookTemplate(facebookService.getPageCachedToken(pageId).accessToken());
            FileData fileData = fileService.processUploadedFile(file);
            Resource resource = new FileSystemResource(fileData.getPath());
            return facebookTemplate.mediaOperations().postPhoto(resource);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
