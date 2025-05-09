package com.MarketingMVP.AllVantage.Controllers.Accounts;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.MetaAuth.MetaAuthService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account/facebook")
public class FacebookController {
    private final FacebookService facebookService;
    private final FileService fileService;
    private final MetaAuthService metaAuthService;

    public FacebookController(FacebookService facebookService, FileService fileService, MetaAuthService metaAuthService) {
        this.facebookService = facebookService;
        this.fileService = fileService;
        this.metaAuthService = metaAuthService;
    }

    @GetMapping("/add-global-account")
    public RedirectView facebookAuth() {
        return metaAuthService.authenticateGlobalAccount();
    }

    @GetMapping("/callback")
    public ResponseEntity<Object> facebookCallback(@RequestParam("code") String code) {
        return metaAuthService.authenticateGlobalAccountCallback(code);
    }

    @GetMapping("/{accountId}/user-pages")
    public ResponseEntity<Object> getUserPages(@PathVariable Long accountId) {
        return facebookService.getUserPages(accountId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllAccounts() {
        return facebookService.getAllAccounts();
    }

    @PostMapping("/{pageId}/post")
    public ResponseEntity<PlatformPostResult> createPost(
            @PathVariable Long pageId,
            @RequestParam String content,
            @RequestParam List<MultipartFile> files,
            @RequestParam @Nullable Date scheduledAt,
            @RequestParam String title
    ) {
        try {
            List<FileData> fileDataList = files.stream().map((file) -> {
                try {
                    return fileService.processUploadedFile(file);
                } catch (Exception e) {
                    return null;
                }
            }).toList();
            PlatformPostResult result = facebookService.createFacebookPost(fileDataList, title, content, scheduledAt, pageId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @PostMapping("/{pageId}/reel")
    public ResponseEntity<PlatformPostResult> postReel(
            @PathVariable Long pageId,
            @RequestParam String content,
            @RequestParam MultipartFile video,
            @RequestParam @Nullable Date scheduledAt,
            @RequestParam String title
    ) {
        try {
            FileData fileData = fileService.processUploadedFile(video);
            PlatformPostResult result = facebookService.createFacebookReel(fileData, title, content, scheduledAt, pageId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @PostMapping("/{pageId}/story")
    public ResponseEntity<PlatformPostResult> postStory(
            @PathVariable Long pageId,
            @RequestParam String content,
            @RequestParam MultipartFile story,
            @RequestParam @Nullable Date scheduledAt,
            @RequestParam String title
    ) {
        try {
            FileData fileData = fileService.processUploadedFile(story);
            PlatformPostResult result = facebookService.storyOnFacebookPage(fileData, title, content, scheduledAt, pageId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @GetMapping("/{pageId}/insights")
    public ResponseEntity<Object> getPageInsights(
            @PathVariable Long pageId,
            @RequestParam String metricName
    ) {
        try {
            PlatformInsightsResult result = facebookService.getFacebookPageInsights(pageId, metricName);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @GetMapping("/{pageId}/{postId}/insights")
    public ResponseEntity<Object> getPostInsights(
            @PathVariable Long pageId,
            @PathVariable String postId,
            @RequestParam String metricList
    ) {
        try {
            PlatformInsightsResult result = facebookService.getFacebookPostInsights(pageId, postId, metricList);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @GetMapping("/{pageId}/posts")
    public ResponseEntity<Object> getPosts(@PathVariable Long pageId) {
        try {
            return facebookService.getAllPosts(pageId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @PostMapping("/{accountId}/refresh")
    public ResponseEntity<Object> refreshToken(@PathVariable Long accountId) {
        return facebookService.testRefreshMethod(accountId);
    }
}
