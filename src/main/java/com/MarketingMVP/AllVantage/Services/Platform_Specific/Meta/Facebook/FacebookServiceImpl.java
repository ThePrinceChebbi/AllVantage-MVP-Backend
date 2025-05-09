package com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookTokenType;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookPageRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook.FacebookMediaRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook.FacebookPostRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook.FacebookReelRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook.FacebookStoryRepository;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.MetaAuth.MetaAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings({"rawtypes", "CallToPrintStackTrace"})
@Service
public class FacebookServiceImpl implements FacebookService {

    private final FileService fileService;
    private final FacebookMediaRepository facebookMediaRepository;
    private final FacebookStoryRepository facebookStoryRepository;
    private final FacebookReelRepository facebookReelRepository;
    private final FacebookPostRepository facebookPostRepository;
    private final FacebookAccountRepository facebookAccountRepository;
    private final FacebookPageRepository facebookPageRepository;
    private final MetaAuthService metaAuthService;

    public FacebookServiceImpl(RedisTemplate<String, FacebookAccountTokenDTO> redisAccountTemplate, MetaAuthService facebookOAuthTokenService, FacebookAccountRepository facebookAccountRepository, FacebookPageRepository facebookPageRepository, FileService fileService, FacebookMediaRepository facebookMediaRepository, FacebookStoryRepository facebookStoryRepository, FacebookReelRepository facebookReelRepository, FacebookPostRepository facebookPostRepository, MetaAuthService metaAuthService) {
        this.facebookAccountRepository = facebookAccountRepository;
        this.facebookPageRepository = facebookPageRepository;
        this.fileService = fileService;
        this.facebookMediaRepository = facebookMediaRepository;
        this.facebookStoryRepository = facebookStoryRepository;
        this.facebookReelRepository = facebookReelRepository;
        this.facebookPostRepository = facebookPostRepository;
        this.metaAuthService = metaAuthService;
    }

    @Override
    public FacebookMedia uploadMediaToFacebook(FileData fileData, Long pageId) {
        FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);
        RestTemplate restTemplate = new RestTemplate();

        String url = String.format("https://graph.facebook.com/v22.0/%s/photos", tokenDTO.facebookPageId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Read the file & send it directly
        File file = new File(fileData.getPath());
        FileSystemResource fileResource = new FileSystemResource(file);
        body.add("source", fileResource);
        body.add("published", "false");
        body.add("access_token", tokenDTO.accessToken());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        FacebookMedia facebookMedia = new FacebookMedia(
                Objects.requireNonNull(response.getBody()).get("id").toString(),
                fileData,
                PlatformMediaType.IMAGE
        );

        return facebookMediaRepository.save(facebookMedia);
    }

    @Override
    public PlatformPostResult createFacebookPost(List<FileData> files, String title, String content, Date scheduledAt, Long pageId) {
        try {
            FacebookPage facebookPage = facebookPageRepository.findById(pageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Facebook page not found with ID: " + pageId));
            List<FileData> videoFiles = files.stream().filter(fileData -> fileData.getType().equals("video")).toList();
            List<FileData> imageFiles = files.stream().filter(fileData -> fileData.getType().equals("image")).toList();
            if (videoFiles.size()==1 && imageFiles.isEmpty()){
                return postVideo(videoFiles.get(0), title, content, facebookPage);
            } else if (videoFiles.isEmpty()) {
                return makePostWithImages(imageFiles, title, content, facebookPage);
            }else {
                return PlatformPostResult.failure(PlatformType.FACEBOOK, "Either one video, one or more images, or no media is allowed");
            }

        } catch (Exception e) {
                files.forEach(file -> {
                    try {
                        fileService.deleteFileFromFileSystem(file);
                    } catch (IOException ex) {
                        System.out.println("Failed to delete video file: " + ex.getMessage());
                    }
                }
            );
                System.out.println(e.getMessage());
            return PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage());
        }
    }

    @Override
    public PlatformPostResult createFacebookReel(FileData videoFile, String title, String content, Date scheduledAt, Long pageId) {
        try {
            FacebookPage facebookPage = facebookPageRepository.findById(pageId)
                    .orElseThrow( () -> new ResourceNotFoundException("Facebook page not found with ID: " + pageId));

            String filename = videoFile.getPrefix();
            if (!filename.matches(".*\\.(mp4|avi|mov|mkv|webm|flv|wmv|mpeg|3gp)$")) {
                return PlatformPostResult.failure(PlatformType.FACEBOOK, "Video file is required for Reel post.");
            }
            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);

            String videoId = initiateVideo(tokenDTO, false);
            File file = fileService.getFileFromFileData(videoFile);
            ResponseEntity<String> uploadResponse = uploadVideo(file, tokenDTO, videoId);
            if (!uploadResponse.getStatusCode().is2xxSuccessful()) return PlatformPostResult.failure(PlatformType.FACEBOOK, "Failed to upload video." + uploadResponse.getBody());

            RestTemplate restTemplate = new RestTemplate();
            // Step 2: Post the uploaded video as a Reel
            String reelUrl = String.format("https://graph.facebook.com/v22.0/%s/video_reels", tokenDTO.facebookPageId());
            Map<String, Object> reelBody = new HashMap<>();
            reelBody.put("description", content);
            reelBody.put("video_id", videoId);
            reelBody.put("access_token", tokenDTO.accessToken());
            reelBody.put("upload_phase","finish");
            reelBody.put("video_state","PUBLISHED");

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<Map<String, Object>> reelRequest = new HttpEntity<>(reelBody, headers);
            ResponseEntity<Map> reelResponse = restTemplate.exchange(reelUrl, HttpMethod.POST, reelRequest, Map.class);

            if (!reelResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(Objects.requireNonNull(reelResponse.getBody()).toString());
            }

            FacebookReel facebookReel = new FacebookReel(
                    videoId,
                    content,
                    videoFile,
                    PlatformMediaType.VIDEO,
                    facebookPage
            );
            facebookReelRepository.save(facebookReel);
            return PlatformPostResult.success(PlatformType.FACEBOOK, facebookReel);
        } catch (Exception e) {
            try {
                fileService.deleteFileFromFileSystem(videoFile);
            } catch (IOException ex) {
                System.out.println("Failed to delete video file: " + ex.getMessage());
            }
            return PlatformPostResult.failure(PlatformType.FACEBOOK, "Failed to post Reel: " + e.getMessage());
        }
    }

    @Override
    public PlatformPostResult storyOnFacebookPage(FileData story, String title, String content, Date scheduledAt, Long facebookPageId) {
        try {
            if (story == null) {
                return PlatformPostResult.failure(PlatformType.FACEBOOK, "Story file is required for Story post.");
            }

            boolean isImage = story.getType().contains("image");

            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(facebookPageId);
            FacebookMedia facebookMedia;
            if (!isImage){
                String videoId = initiateVideo(tokenDTO, true);
                File file = fileService.getFileFromFileData(story);

                ResponseEntity<String> uploadResponse = uploadVideo(file, tokenDTO, videoId);
                if (!uploadResponse.getStatusCode().is2xxSuccessful())
                    return PlatformPostResult.failure(PlatformType.FACEBOOK, "Failed to upload video." + uploadResponse.getBody());
                facebookMedia = new FacebookMedia(
                        videoId,
                        story,
                        PlatformMediaType.VIDEO
                );
            }else {
                facebookMedia = uploadMediaToFacebook(story, facebookPageId);
            }
            RestTemplate restTemplate = new RestTemplate();
            String urlEnding = isImage ? "photo_stories" : "video_stories";
            String storyUrl = String.format("https://graph.facebook.com/v22.0/%s/%s", tokenDTO.facebookPageId(), urlEnding);
            MultiValueMap<String, Object> storyBody = new LinkedMultiValueMap<>();
            storyBody.add("access_token", tokenDTO.accessToken());
            if (!isImage){
                storyBody.add("upload_phase", "finish");
                storyBody.add("video_id", facebookMedia.getMediaId());
            }else {
                storyBody.add("photo_id",facebookMedia.getMediaId());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(storyBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(storyUrl, HttpMethod.POST, requestEntity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create story post: " + response);
            }

            facebookMediaRepository.save(facebookMedia);
            FacebookPage facebookPage = facebookPageRepository.findById(facebookPageId)
                    .orElseThrow( () -> new ResourceNotFoundException("Facebook page not found with ID: " + facebookPageId));
            FacebookStory facebookStory = new FacebookStory(
                    Objects.requireNonNull(response.getBody()).get("post_id").toString(),
                    facebookMedia,
                    facebookPage
            );
            facebookStoryRepository.save(facebookStory);

            return PlatformPostResult.success(PlatformType.FACEBOOK, facebookStory);
        } catch (Exception e) {
            try {
                fileService.deleteFileFromFileSystem(story);
            } catch (IOException ex) {
                System.out.println("Failed to delete video file: " + ex.getMessage());
            }
            return PlatformPostResult.failure(PlatformType.FACEBOOK, "Failed to post Story: " + e.getMessage());
        }
    }

    @Override
    public PlatformInsightsResult getFacebookPageInsights(Long pageId, String metricName) {
        try {
            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);

            String insightsUrl = String.format(
                    "https://graph.facebook.com/v22.0/%s/insights/%s?access_token=%s",
                    tokenDTO.facebookPageId(),
                    metricName,
                    tokenDTO.accessToken()
            );

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(insightsUrl, HttpMethod.GET, null, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to fetch insights: " + response.getBody());
            }
            return PlatformInsightsResult.success(PlatformType.FACEBOOK, Objects.requireNonNull(response.getBody()));
        } catch (Exception e) {
            return PlatformInsightsResult.failure(PlatformType.FACEBOOK, "Failed to fetch insights: " + e.getMessage());
        }
    }

    @Override
    public PlatformInsightsResult getFacebookPostInsights(Long pageId, String facebookPostId, String metricList) {
        try {
            FacebookPost facebookPost = facebookPostRepository.findById(facebookPostId)
                    .orElseThrow(() -> new ResourceNotFoundException("Facebook post not found with ID: " + facebookPostId));

            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(pageId);
            String insightsUrl = String.format(
                    "https://graph.facebook.com/v22.0/%s/insights/?access_token=%s&metric=%s",
                    facebookPostId,
                    tokenDTO.accessToken(),
                    metricList
            );

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(insightsUrl, HttpMethod.GET, null, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to fetch insights: " + response.getBody());
            }
            Map<String, Object> insights = new HashMap<>();
            insights.put("post", facebookPost);
            insights.put("insights", response.getBody());
            return PlatformInsightsResult.success(PlatformType.FACEBOOK, insights);
        } catch (Exception e) {
            return PlatformInsightsResult.failure(PlatformType.FACEBOOK, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAllPosts(Long pageId){
        return ResponseEntity.ok(facebookPostRepository.findAllByPageId(pageId));
    }

    @Override
    public ResponseEntity<Object> testRefreshMethod(Long accountId) {
        try{
            return metaAuthService.refreshAllTokens(metaAuthService.fetchAccountToken(accountId, FacebookTokenType.FACEBOOK_LONG_LIVED));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error refreshing tokens: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getUserPages(Long accountId) {
        try {
            JsonNode userPages = fetchUserPages(accountId);

            List<FacebookPageDTO> pageList = new ArrayList<>();

            if (userPages.has("data")) {
                for (JsonNode page : userPages.get("data")) {
                    FacebookPageDTO dto = new FacebookPageDTO(
                            page.get("id").asText(),
                            page.get("name").asText()
                    );
                    pageList.add(dto);
                }
            }

            return ResponseEntity.ok(pageList);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching user pages: " + e.getMessage());
        }
    }

    //Utility public methods -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public JsonNode fetchUserPages(Long accountId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookAccountTokenDTO token;

        token = metaAuthService.getAccountCachedToken(accountId, FacebookTokenType.FACEBOOK_LONG_LIVED);

        String url = "https://graph.facebook.com/v19.0/me/accounts?access_token=" + token.accessToken();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch managed pages. Status: " + response.getStatusCode());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    @Override
    public ResponseEntity<Object> getAllAccounts() {
        return ResponseEntity.ok(facebookAccountRepository.findAll());
    }

    @Override
    public PlatformInsightsResult getFacebookInsights(Long id, Date startDate, Date endDate) {
        return null;
    }

    //Utility private methods -------------------------------------------------------------------------------------------------------------------------------

    private String initiateVideo(FacebookPageTokenDTO tokenDTO, boolean isStory){
        try {
            RestTemplate restTemplate = new RestTemplate();
            String uploadUrl = isStory ?
                    String.format("https://graph.facebook.com/v22.0/%s/video_stories", tokenDTO.facebookPageId())
                    : String.format("https://graph.facebook.com/v22.0/%s/videos", tokenDTO.facebookPageId());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("access_token", tokenDTO.accessToken());
            body.add("upload_phase", "start");

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.POST, request, Map.class);
            return Objects.requireNonNull(uploadResponse.getBody()).get("video_id").toString();
        }catch (Exception e){
            System.out.println("error: " + e.getMessage()); // Consider proper logging
            return "error initiating video";
        }
    }

    private ResponseEntity<String> uploadVideo(File videoFile, FacebookPageTokenDTO tokenDTO, String videoId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add("Authorization", "OAuth " + tokenDTO.accessToken());
            headers.add("offset", "0");
            headers.add("file_size", String.valueOf(videoFile.length()));

            // Ensure Content-Length is set
            headers.setContentLength(videoFile.length());

            HttpEntity<FileSystemResource> request = new HttpEntity<>(new FileSystemResource(videoFile), headers);

            // Optional: Delete file after upload
            return restTemplate.exchange(
                    String.format("https://rupload.facebook.com/video-upload/v22.0/%s", videoId),
                    HttpMethod.POST,
                    request,
                    String.class
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload video: " + e.getMessage());
        }
    }

    private PlatformPostResult postVideo(FileData fileData, String title, String content, FacebookPage facebookPage) {
        try {
            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(facebookPage.getId());

            System.out.println(tokenDTO.accessToken());

            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("https://graph.facebook.com/v22.0/%s/videos", tokenDTO.facebookPageId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            FileSystemResource resource = new FileSystemResource(fileData.getPath());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("access_token", tokenDTO.accessToken());
            body.add("title", title);
            body.add("description", content);
            body.add("resource", resource);
            /*if (scheduledAt != null) {
                body.add("scheduled_publish_time", String.valueOf(scheduledAt.getTime() / 1000));
                body.add("published", "false");
            } else {*/
                body.add("published", "true");


            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            System.out.println(response.getBody());
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create video post: " + response.getBody());
            }
            FacebookMedia media = new FacebookMedia(
                    Objects.requireNonNull(response.getBody()).get("id").toString(),
                    fileData,
                    PlatformMediaType.VIDEO
            );
            FacebookPost post = new FacebookPost(
                    Objects.requireNonNull(response.getBody()).get("id").toString(),
                    content,
                    List.of(facebookMediaRepository.save(media)),
                    facebookPage
            );
            return PlatformPostResult.success(PlatformType.FACEBOOK, facebookPostRepository.save(post));
        } catch (Exception e) {
            return PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage());
        }
    }

    private PlatformPostResult makePostWithImages(List<FileData> files, String title, String content, FacebookPage facebookPage) {
        try {
            FacebookPageTokenDTO tokenDTO = metaAuthService.getPageCachedToken(facebookPage.getId());

            System.out.println(tokenDTO.accessToken());
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("https://graph.facebook.com/v22.0/%s/feed", tokenDTO.facebookPageId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ExecutorService executor = Executors.newFixedThreadPool(files.size()); // Adjust pool size as needed
            List<Callable<FacebookMedia>> tasks = new ArrayList<>();
            List<FacebookMedia> mediaList = new ArrayList<>();
            for (FileData fileData : files) {
                tasks.add(() ->
                        uploadMediaToFacebook(fileData, facebookPage.getId())
                );
            }
            List<Future<FacebookMedia>> futures = executor.invokeAll(tasks);
            for (Future<FacebookMedia> future : futures) {
                try {
                    mediaList.add(future.get());
                } catch (ExecutionException e) {
                    System.out.println(e.getMessage());
                }
            }

            executor.shutdown();

            List<Map<String, String>> attachedMedia = mediaList.stream()
                    .map(media -> Map.of("media_fbid", media.getMediaId()))
                    .toList();

            Map<String, Object> body = new HashMap<>();
            body.put("title", title);
            body.put("message", content);
            body.put("attached_media", attachedMedia);
            body.put("access_token", tokenDTO.accessToken());  // Use the correct Page Access Token

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            FacebookPost post = new FacebookPost(
                    Objects.requireNonNull(response.getBody()).get("id").toString().split("_")[1],
                    content,
                    mediaList,
                    facebookPage
            );
            return PlatformPostResult.success(PlatformType.FACEBOOK, facebookPostRepository.save(post));
        }catch (Exception e){
            return PlatformPostResult.failure(PlatformType.FACEBOOK, e.getMessage());
        }
    }
}
