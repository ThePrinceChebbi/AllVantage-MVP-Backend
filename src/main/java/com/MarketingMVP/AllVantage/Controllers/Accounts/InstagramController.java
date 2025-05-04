package com.MarketingMVP.AllVantage.Controllers.Accounts;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Instagram.InstagramService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account/instagram")
public class InstagramController {

    private final InstagramService instagramService;
    private final FileService fileService;

    public InstagramController(InstagramService instagramService, FileService fileService) {
        this.instagramService = instagramService;
        this.fileService = fileService;
    }

    @GetMapping("/{pageId}/ig-accounts")
    public ResponseEntity<Object> getUserPages(@PathVariable Long pageId) {
        return instagramService.getPageInstagramAccounts(pageId);
    }

    @GetMapping("/{pageId}/{igId}")
    public ResponseEntity<Object> getAccountDetails(@PathVariable Long pageId, @PathVariable String igId) {
        return instagramService.getInstagramAccountDetails(igId,pageId);
    }

    @PostMapping("/{pageId}/add")
    public ResponseEntity<Object> addAccount(@PathVariable Long pageId, @RequestParam String igId) {
        return instagramService.addInstagramAccount(igId,pageId);
    }

    @GetMapping("/get_all")
    public ResponseEntity<Object> getAllAccounts() {
        return instagramService.getAllAccounts();
    }

    @PostMapping("/{accountId}/post")
    public ResponseEntity<PlatformPostResult> createPost(
            @PathVariable Long accountId,
            @RequestParam String content,
            @RequestParam List<MultipartFile> files,
            @RequestParam @Nullable Date scheduledAt,
            @RequestParam String title) {
        try {
            List<FileData> fileDataList = files.stream().map((file) -> {
                try {
                    return fileService.processUploadedFile(file);
                } catch (Exception e) {
                    return null;
                }
            }).toList();
            PlatformPostResult result = instagramService.createInstagramPost(fileDataList, title, content, scheduledAt, accountId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }

    @PostMapping("/{accountId}/reel")
    public ResponseEntity<PlatformPostResult> postReel(
            @PathVariable Long accountId,
            @RequestParam String content,
            @RequestParam MultipartFile video,
            @RequestParam @Nullable Date scheduledAt
    ) {
        try {
            FileData fileData = fileService.processUploadedFile(video);
            PlatformPostResult result = instagramService.createInstagramReel(fileData, content, scheduledAt, accountId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }

    @PostMapping("/{accountId}/story")
    public ResponseEntity<PlatformPostResult> postStory(
            @PathVariable Long accountId,
            @RequestParam MultipartFile story,
            @RequestParam @Nullable Date scheduledAt,
            @RequestParam String title
    ) {
        try {
            FileData fileData = fileService.processUploadedFile(story);
            PlatformPostResult result = instagramService.createInstagramStory(fileData, scheduledAt, accountId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }


    @GetMapping("/{pageId}/insights")
    public ResponseEntity<Object> getPageInsights(
            @PathVariable Long pageId,
            @RequestParam List<String> metricList,
            @RequestParam String period
    ) {
        try {
            PlatformInsightsResult result = instagramService.getInstagramAccountInsights(pageId, metricList, period);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/posts/{postId}/insights")
    public ResponseEntity<Object> getPostInsights(
            @PathVariable Long accountId,
            @PathVariable String postId,
            @RequestParam List<String> metricList
    ) {
        try {
            PlatformInsightsResult result = instagramService.getInstagramPostInsights(accountId, postId, metricList);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }
    @GetMapping("/{accountId}/reels/{reelId}/insights")
    public ResponseEntity<Object> getReelInsights(
            @PathVariable Long accountId,
            @PathVariable String reelId,
            @RequestParam List<String> metricList
    ) {
        try {
            PlatformInsightsResult result = instagramService.getInstagramReelsInsights(accountId, reelId, metricList);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/posts")
    public ResponseEntity<Object> getPosts(@PathVariable Long accountId) {
        try {
            return instagramService.getAllPosts(accountId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/reels")
    public ResponseEntity<Object> getReels(@PathVariable Long accountId) {
        try {
            return instagramService.getAllReels(accountId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage()));
        }
    }
}
