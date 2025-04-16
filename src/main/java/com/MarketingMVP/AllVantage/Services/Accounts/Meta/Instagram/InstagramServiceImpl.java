package com.MarketingMVP.AllVantage.Services.Accounts.Meta.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Instagram.InstagramAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookPageRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.Instagram.InstagramAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Instagram.InstagramMediaRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Instagram.InstagramPostRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Instagram.InstagramReelRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Instagram.InstagramStoryRepository;
import com.MarketingMVP.AllVantage.Services.Accounts.Meta.MetaAuth.MetaAuthService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
@Service
public class InstagramServiceImpl implements InstagramService{

    private final String ngrokUrl = "https://99bf-197-19-175-124.ngrok-free.app/";

    private final MetaAuthService metaAuthService;
    private final FacebookPageRepository facebookPageRepository;
    private final InstagramAccountRepository instagramAccountRepository;
    private final FileService fileService;
    private final InstagramPostRepository instagramPostRepository;
    private final InstagramMediaRepository instagramMediaRepository;
    private final InstagramReelRepository instagramReelRepository;
    private final InstagramStoryRepository instagramStoryRepository;

    public InstagramServiceImpl(MetaAuthService metaAuthService, FacebookPageRepository facebookPageRepository, InstagramAccountRepository instagramAccountRepository, FileService fileService, InstagramPostRepository instagramPostRepository, InstagramMediaRepository instagramMediaRepository, InstagramReelRepository instagramReelRepository, InstagramStoryRepository instagramStoryRepository) {
        this.metaAuthService = metaAuthService;
        this.facebookPageRepository = facebookPageRepository;
        this.instagramAccountRepository = instagramAccountRepository;
        this.fileService = fileService;
        this.instagramPostRepository = instagramPostRepository;
        this.instagramMediaRepository = instagramMediaRepository;
        this.instagramReelRepository = instagramReelRepository;
        this.instagramStoryRepository = instagramStoryRepository;
    }

    @Override
    public ResponseEntity<Object> getPageInstagramAccounts(Long pageId) {
        FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);

        String url = String.format("https://graph.facebook.com/v22.0/%s?fields=instagram_business_account&access_token=%s",
                tokenDTO.facebookPageId(),
                tokenDTO.accessToken());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode instaAccount = root.path("instagram_business_account");

            if (instaAccount.isMissingNode()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No Instagram Business account connected to this Page.");
            }

            String instagramId = instaAccount.path("id").asText();
            return ResponseEntity.ok(Map.of("instagram_id", instagramId));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getInstagramAccountDetails(String instagramId, Long pageId) {
        FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);
        String url = String.format(
                "https://graph.facebook.com/v22.0/%s?fields=id,username,profile_picture_url,followers_count,follows_count,media_count,biography,website,name&access_token=%s",
                instagramId,
                tokenDTO.accessToken()
        );

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            return ResponseEntity.ok(Map.of(
                    "id", json.path("id").asText(),
                    "username", json.path("username").asText(),
                    "name", json.path("name").asText(""),
                    "biography", json.path("biography").asText(""),
                    "website", json.path("website").asText(""),
                    "profile_picture_url", json.path("profile_picture_url").asText(""),
                    "followers_count", json.path("followers_count").asInt(0),
                    "follows_count", json.path("follows_count").asInt(0),
                    "media_count", json.path("media_count").asInt(0)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Object> addInstagramAccount(String instagramBusinessId, Long pageId) {
        FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);
        if (instagramAccountRepository.findInstagramAccountByInstagramId(instagramBusinessId).isPresent()) {
            throw new IllegalArgumentException(String.format("Instagram Account with instagram id: %s already exists.",instagramBusinessId));
        }
        FacebookPage facebookPage = facebookPageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        String url = String.format(
                "https://graph.facebook.com/v22.0/%s?fields=username,name&access_token=%s",
                instagramBusinessId,
                tokenDTO.accessToken()
        );

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            String username = json.path("username").asText();
            String name = json.path("name").asText("");

            InstagramAccount instagramAccount = new InstagramAccount(
                    instagramBusinessId,
                    name.isEmpty() ? username : name,
                    new Date(),
                    new Date(),
                    facebookPage
            );

            InstagramAccount savedAccount = instagramAccountRepository.save(instagramAccount);

            return ResponseEntity.ok(Map.of(
                    "message", "Instagram account added successfully",
                    "instagram_id", instagramBusinessId,
                    "account", new InstagramAccountDTOMapper().apply(savedAccount)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public PlatformPostResult createInstagramPost(List<FileData> files, String title, String caption, Date scheduledAt, Long instagramAccountId) {
        try {
            InstagramAccount instagramAccount = instagramAccountRepository.findById(instagramAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Instagram account not found with ID: " + instagramAccountId));
            return postInstagramMedia(files, caption, scheduledAt, instagramAccount);
        } catch (Exception e) {
            files.forEach(file -> {
                try {
                    fileService.deleteFileFromFileSystem(file);
                } catch (IOException ex) {
                    System.out.println("Failed to delete media file: " + ex.getMessage());
                }
            });
            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
        }
    }

    private PlatformPostResult postInstagramMedia(List<FileData> files, String caption, @Nullable Date scheduledAt, InstagramAccount instagramAccount) {
        try {
            if (files.size() > 10) {
                throw new IllegalArgumentException("Instagram carousel supports up to 10 media files.");
            }

            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(instagramAccount.getFacebookPage().getId());
            System.out.println("Token retrieved successfully for page ID: " + instagramAccount.getFacebookPage().getId());

            List<String> creationIds = new ArrayList<>();
            List<InstagramMedia> mediaList = new ArrayList<>();

            if(files.size()==1){
                System.out.println(files);
                FileData file = files.get(0);
                String creationId = uploadMediaItem(file, instagramAccount, tokenDTO.accessToken(),caption, false);
                System.out.println("Media item uploaded with creation ID: " + creationId);

                PlatformMediaType mediaType = "image".equals(file.getType()) ? PlatformMediaType.IMAGE : PlatformMediaType.VIDEO;
                InstagramMedia media = instagramMediaRepository.save(new InstagramMedia(creationId, file, mediaType));

                String postId = publishCarousel(creationId, instagramAccount, tokenDTO.accessToken(), caption);
                mediaList.add(media);
                InstagramPost post = new InstagramPost(
                        postId,
                        caption,
                        mediaList,
                        instagramAccount
                );
                return PlatformPostResult.success(PlatformType.INSTAGRAM, instagramPostRepository.save(post));
            }
            int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), files.size());
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            try {
                List<CompletableFuture<PlatformPostResult>> futures = new ArrayList<>();

                for (FileData file : files) {
                    // Submit each file upload as an async task
                    CompletableFuture<PlatformPostResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            String creationId = withRetry(
                                    () -> uploadMediaItem(file, instagramAccount, tokenDTO.accessToken(),caption, false),
                                    3, 1000,
                                    "uploadMediaItem for " + file.getId()
                            );
                            waitForMediaProcessing(creationId, tokenDTO.accessToken());
                            PlatformMediaType mediaType = "image".equals(file.getType()) ?
                                    PlatformMediaType.IMAGE : PlatformMediaType.VIDEO;

                            return PlatformPostResult.success(PlatformType.INSTAGRAM,instagramMediaRepository.save(new InstagramMedia(creationId, file, mediaType)));
                        } catch (Exception e) {
                            System.out.println("Failed to upload media: " + e.getMessage());
                            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
                        }
                    }, executor);

                    futures.add(future);
                }

                // Wait for all uploads to complete and collect results
                CompletableFuture<Void> allOf = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));

                // Get the result and add to creationIds and mediaList
                allOf.thenRun(() -> {
                    for (CompletableFuture<PlatformPostResult> future : futures) {
                        try {
                            if (future.get().isSuccess() && future.get().getResult() instanceof InstagramMedia media){
                                creationIds.add(media.getMediaId());
                                mediaList.add(media);
                            }
                        } catch (Exception e) {
                            System.out.println("Failed to upload media: " + e.getMessage());
                        }
                    }
                }).join(); // Wait for the processing to complete

            } finally {
                executor.shutdown();
            }

            String carouselCreationId = createCarousel(creationIds, caption, scheduledAt, instagramAccount, tokenDTO.accessToken());

            String finalPostId = publishCarousel(carouselCreationId, instagramAccount, tokenDTO.accessToken(),caption);

            InstagramPost post = new InstagramPost(finalPostId, caption, mediaList, instagramAccount);
            InstagramPost savedPost = instagramPostRepository.save(post);

            return PlatformPostResult.success(PlatformType.INSTAGRAM, savedPost);
        } catch (Exception e) {
            System.out.println("ERROR in postInstagramMedia: " + e.getMessage());
            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
        }
    }

    private String uploadMediaItem(FileData file, InstagramAccount instagramAccount, String accessToken, @Nullable String caption, boolean isStory) {
        String endpoint = buildUploadEndpoint(file, instagramAccount, accessToken,caption, isStory);
        HttpEntity<?> request = new HttpEntity<>(buildHeaders());
        ResponseEntity<Map> response = new RestTemplate().postForEntity(endpoint, request, Map.class);
        return Objects.requireNonNull(response.getBody()).get("id").toString();
    }
    private void waitForMediaProcessing(String creationId, String accessToken) throws InterruptedException {
        String url = String.format("https://graph.facebook.com/v22.0/%s?fields=status_code&access_token=%s", creationId, accessToken);
        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < 10; i++) {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            String status = (String) ((Map) response.getBody()).get("status_code");
            System.out.println(creationId + ": " + status);
            if ("FINISHED".equals(status)) return;

            Thread.sleep(1000); // wait 1 second
        }
        throw new IllegalStateException("Media processing timed out for ID: " + creationId);
    }
    private void waitForVideoUpload(String creationId, String accessToken) throws InterruptedException {
        String url = String.format("https://graph.facebook.com/v22.0/%s?fields=status_code&access_token=%s", creationId, accessToken);
        RestTemplate restTemplate = new RestTemplate();

        boolean isFinished = false;
        while (!isFinished) {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            String status = (String) ((Map) response.getBody()).get("status_code");
            System.out.println(creationId + ": " + status);
            if ("FINISHED".equals(status)) isFinished = true;

            Thread.sleep(3000); // wait 1 second
        }
    }
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private String buildUploadEndpoint(FileData file, InstagramAccount account, String accessToken, @Nullable String caption, boolean isStory) {
        String mediaUrl = ngrokUrl + "api/v1/files/" + file.getId() ;
        String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String mediaType = isStory ? "STORIES" : "REELS";
        return switch (file.getType()) {
            case "image" -> String.format(
                    "https://graph.facebook.com/v22.0/%s/media?image_url=%s&access_token=%s",
                    account.getInstagramId(), mediaUrl, encodedToken
            );
            case "video" -> String.format(
                    "https://graph.facebook.com/v22.0/%s/media?video_url=%s&caption=%s&media_type=%s&access_token=%s",
                    account.getInstagramId(), mediaUrl, (caption != null) ? caption : "", mediaType , encodedToken
            );
            default -> throw new IllegalArgumentException("Unsupported media type: " + file.getType());
        };
    }

    private String createCarousel(List<String> children, String caption, @Nullable Date scheduledAt, InstagramAccount account, String accessToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("caption", caption);
        body.add("media_type", "CAROUSEL");  // Specify the media type as CAROUSEL
        body.add("children", String.join(",", children));
        body.add("access_token", accessToken);

        if (scheduledAt != null) {
            body.add("published", "false");
            body.add("scheduled_publish_time", String.valueOf(scheduledAt.getTime() / 1000));
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<Map> response = new RestTemplate().postForEntity(
                String.format("https://graph.facebook.com/v22.0/%s/media", account.getInstagramId()), request, Map.class
        );

        return Objects.requireNonNull(response.getBody()).get("id").toString();
    }

    private String publishCarousel(String creationId, InstagramAccount account, String accessToken, @Nullable String caption) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("creation_id", creationId);
        body.add("access_token", accessToken);
        if (caption !=null ) body.add("caption", caption);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<Map> response = new RestTemplate().postForEntity(
                String.format("https://graph.facebook.com/v22.0/%s/media_publish", account.getInstagramId()), request, Map.class
        );

        return Objects.requireNonNull(response.getBody()).get("id").toString();
    }

    @Override
    public PlatformPostResult createInstagramReel(FileData video, String caption, Date scheduledAt, Long instagramAccountId) {
        try {
            InstagramAccount instagramAccount = instagramAccountRepository.findById(instagramAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Instagram account not found with ID: " + instagramAccountId));

            return postInstagramReel(video, caption, scheduledAt, instagramAccount);

        } catch (Exception e) {
            try {
                fileService.deleteFileFromFileSystem(video);
            } catch (IOException ex) {
                System.out.println("Failed to delete video file: " + ex.getMessage());
            }

            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
        }
    }
    private PlatformPostResult postInstagramReel(FileData video, String caption, Date scheduledAt, InstagramAccount account ){
        try{
            if (!video.getType().contains("video")) {
                return PlatformPostResult.failure(
                        PlatformType.INSTAGRAM,
                        "Provided file is not a video file"
                );
            }
            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(account.getFacebookPage().getId());

            String mediaId = uploadMediaItem(
                    video,
                    account,
                    tokenDTO.accessToken(),
                    caption,
                    false
            );

            // Wait for processing if needed
            waitForVideoUpload(mediaId, tokenDTO.accessToken());

            InstagramMedia media = new InstagramMedia(
                    mediaId,
                    video,
                    PlatformMediaType.VIDEO
            );
            // Publish the reel
            String publishedReelId = publishCarousel(
                    mediaId,
                    account,
                    tokenDTO.accessToken(),
                    caption
            ); // Already used for single media too

            InstagramReel instagramReel = new InstagramReel(
                    publishedReelId,
                    caption,
                    instagramMediaRepository.save(media),
                    account
            );

            return PlatformPostResult.success(PlatformType.INSTAGRAM, instagramReelRepository.save(instagramReel));
        }catch (Exception e){
            System.out.println("Failed to post instagram reel: " + e.getMessage());
            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
        }
    }


    @Override
    public PlatformPostResult createInstagramStory(FileData media , Date scheduledAt, Long instagramAccountId) {
        try {
            InstagramAccount instagramAccount = instagramAccountRepository.findById(instagramAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Instagram account not found with ID: " + instagramAccountId));

            return postInstagramStory(media, scheduledAt, instagramAccount);

        } catch (Exception e) {
            try {
                fileService.deleteFileFromFileSystem(media);
            } catch (IOException ex) {
                System.out.println("Failed to delete video file: " + ex.getMessage());
            }

            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
        }
    }
    private PlatformPostResult postInstagramStory(FileData fileData, Date scheduledAt, InstagramAccount account ){
        try{
            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(account.getFacebookPage().getId());

            String mediaId = uploadMediaItem(
                    fileData,
                    account,
                    tokenDTO.accessToken(),
                    null,
                    true
            );

            // Wait for processing if needed
            waitForVideoUpload(mediaId, tokenDTO.accessToken());

            InstagramMedia media = new InstagramMedia(
                    mediaId,
                    fileData,
                    PlatformMediaType.VIDEO
            );
            // Publish the reel
            String publishedReelId = publishCarousel(
                    mediaId,
                    account,
                    tokenDTO.accessToken(),
                    null
            ); // Already used for single media too

            InstagramStory instagramStory = new InstagramStory(
                    publishedReelId,
                    instagramMediaRepository.save(media),
                    account
            );

            return PlatformPostResult.success(PlatformType.INSTAGRAM, instagramStoryRepository.save(instagramStory));
        }catch (Exception e){
            System.out.println("Failed to post instagram reel: " + e.getMessage());
            return PlatformPostResult.failure(PlatformType.INSTAGRAM, e.getMessage());
        }
    }

    @Override
    public PlatformInsightsResult getInstagramAccountInsights(Long instagramAccountId, List<String> metrics, String period) {
        return null;
    }

    @Override
    public PlatformInsightsResult getInstagramMediaInsights(String mediaId, List<String> metrics) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getAllPosts(Long accountId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getInstagramAccountInfo(Long accountId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getAllAccounts() {
        return ResponseEntity.ok().body(instagramAccountRepository.findAll());
    }

    private <T> T withRetry(Supplier<T> action, int maxAttempts, long initialDelayMillis, String taskDescription) throws Exception {
        long delay = initialDelayMillis;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                if (attempt == maxAttempts) throw e;

                System.err.printf("Attempt %d for %s failed: %s. Retrying in %dms...\n",
                        attempt, taskDescription, e.getMessage(), delay);
                Thread.sleep(delay);
                delay *= 2; // Exponential backoff
            }
        }
        throw new IllegalStateException("Unreachable retry failure logic hit.");
    }

}
