package com.MarketingMVP.AllVantage.Controllers.Accounts;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account/instagram")
public class InstagramController {

    /*@GetMapping("/{accountId}/user-pages")
    public ResponseEntity<Object> getUserPages(@PathVariable Long accountId) {
        return instagramService.getUserPages(accountId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllAccounts() {
        return instagramService.getAllAccounts();
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
            PlatformPostResult result = instagramService.createFacebookPost(fileDataList, title, content, scheduledAt, pageId);
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
            PlatformPostResult result = instagramService.createFacebookReel(fileData, title, content, scheduledAt, pageId);
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
            PlatformPostResult result = instagramService.storyOnFacebookPage(fileData, title, content, scheduledAt, pageId);
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
