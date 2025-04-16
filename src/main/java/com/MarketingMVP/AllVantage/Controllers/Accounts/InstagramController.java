package com.MarketingMVP.AllVantage.Controllers.Accounts;

import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Services.Accounts.Meta.Instagram.InstagramService;
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
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
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
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

/*
    @GetMapping("/{pageId}/insights")
    public ResponseEntity<Object> getPageInsights(
            @PathVariable Long pageId,
            @RequestParam String metricName
    ) {
        try {
            PlatformInsightsResult result = instagramService.getFacebookPageInsights(pageId, metricName);
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
            PlatformInsightsResult result = instagramService.getFacebookPostInsights(pageId, postId, metricList);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }

    @GetMapping("/{pageId}/posts")
    public ResponseEntity<Object> getPosts(@PathVariable Long pageId) {
        try {
            return instagramService.getAllPosts(pageId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage()));
        }
    }*/
}
