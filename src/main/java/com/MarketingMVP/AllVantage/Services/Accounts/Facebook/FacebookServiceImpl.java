package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Response.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookPageRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.Token.FacebookOAuthToken.FacebookOAuthTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.print.attribute.standard.Media;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class FacebookServiceImpl implements FacebookService {

    private final FileService fileService;
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.scope}")
    private String scope;

    private final String redirectUri = "http://localhost:8080/api/v1/account/facebook/callback";

    private final RedisTemplate<String, FacebookAccountTokenDTO> redisAccountTemplate;
    private final RedisTemplate<String, FacebookPageTokenDTO> redisPageTemplate;
    private final FacebookAccountTokenDTOMapper facebookAccountTokenDTOMapper;
    private final FacebookPageTokenDTOMapper facebookPageTokenDTOMapper;
    private final FacebookOAuthTokenService facebookOAuthTokenService;
    private final FacebookAccountRepository facebookAccountRepository;
    private final FacebookPageRepository facebookPageRepository;
    private final AESEncryptionService encryptionService;

    public FacebookServiceImpl(RedisTemplate<String, FacebookAccountTokenDTO> redisAccountTemplate, FacebookOAuthTokenService facebookOAuthTokenService, FacebookAccountRepository facebookAccountRepository, FacebookPageRepository facebookPageRepository, AESEncryptionService encryptionService, FacebookAccountTokenDTOMapper facebookAccountTokenDTOMapper, RedisTemplate<String, FacebookPageTokenDTO> redisPageTemplate, FacebookPageTokenDTOMapper facebookPageTokenDTOMapper, FileService fileService) {
        this.redisAccountTemplate = redisAccountTemplate;
        this.facebookOAuthTokenService = facebookOAuthTokenService;
        this.facebookAccountRepository = facebookAccountRepository;
        this.facebookPageRepository = facebookPageRepository;
        this.encryptionService = encryptionService;
        this.facebookAccountTokenDTOMapper = facebookAccountTokenDTOMapper;
        this.redisPageTemplate = redisPageTemplate;
        this.facebookPageTokenDTOMapper = facebookPageTokenDTOMapper;
        this.fileService = fileService;
    }

    @Override
    public RedirectView getAuthenticationCode(String redirectUri) {
        String authUrl = "https://www.facebook.com/v19.0/dialog/oauth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scope +
                "&response_type=code";
        return new RedirectView(authUrl);
    }

    @Override
    public RedirectView authenticateGlobalAccount() {
        return getAuthenticationCode(redirectUri);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode) {
        try {
            FacebookAccountDTO account = new FacebookAccountDTOMapper().apply(exchangeCodeForToken(authorizationCode, true, redirectUri));
            return ResponseEntity.ok(account);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Override
    public FacebookMedia uploadMediaToFacebook(FileData fileData, Long pageId) {
        FacebookPageTokenDTO tokenDTO = getPageCachedToken(pageId);
        RestTemplate restTemplate = new RestTemplate();

        String metaFileType = fileData.getType().contains("image") ? "photos" : "videos";
        String url = String.format("https://graph.facebook.com/v22.0/%s/%s", tokenDTO.facebookPageId(), metaFileType);

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
                response.getBody().get("id").toString(),
                fileData,
                PlatformMediaType.IMAGE
        );
        return facebookMedia;
    }

    @Transactional
    @Override
    public PlatformPostResult createFacebookPostDirectly(List<MultipartFile> files, String title, String content, Date scheduledAt, Long facebookPageId) {
        try {
            FacebookPageTokenDTO tokenDTO = fetchPageToken(facebookPageId);
            RestTemplate restTemplate = new RestTemplate();

            String url = String.format("https://graph.facebook.com/v22.0/%s/feed", tokenDTO.facebookPageId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            List<FacebookMedia> mediaList = files.stream().map(file -> {
                try {
                    FileData fileData = fileService.processUploadedFile(file, file.getContentType());
                    return uploadMediaToFacebook(fileData, facebookPageId);
                } catch (IOException e) {
                    return null;
                }
            }).toList();

            List<Map<String, String>> attachedMedia = mediaList.stream()
                    .map(media -> Map.of("media_fbid", media.getMediaId()))
                    .toList();

            Map<String, Object> body = new HashMap<>();
            body.put("message", content);
            body.put("attached_media", attachedMedia);
            body.put("access_token", tokenDTO.accessToken());  // Use the correct Page Access Token

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            String message = "Post Created! ID: " + Objects.requireNonNull(response.getBody());
            return PlatformPostResult.success(PlatformType.FACEBOOK_PAGE, message);
        } catch (Exception e) {
            return PlatformPostResult.failure(PlatformType.FACEBOOK_PAGE, "Failed to create post: " + e.getMessage());
        }
    }

    @Override
    public PlatformPostResult createFacebookPost(List<FileData> files, String title, String content, Date scheduledAt, FacebookPage facebookPage) {
        try {
            FacebookPageTokenDTO tokenDTO = fetchPageToken(facebookPage.getId());
            RestTemplate restTemplate = new RestTemplate();

            String url = String.format("https://graph.facebook.com/v22.0/%s/feed", tokenDTO.facebookPageId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            List<FacebookMedia> mediaList = files.stream().map(file -> {
                try {
                    return uploadMediaToFacebook(file, facebookPage.getId());
                } catch (Exception e) {
                    return null;
                }
            }).toList();

            List<Map<String, String>> attachedMedia = mediaList.stream()
                    .map(media -> Map.of("media_fbid", media.getMediaId()))
                    .toList();

            Map<String, Object> body = new HashMap<>();
            body.put("message", content);
            body.put("attached_media", attachedMedia);
            body.put("access_token", tokenDTO.accessToken());  // Use the correct Page Access Token

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            FacebookPost post = new FacebookPost(
                    Objects.requireNonNull(response.getBody()).get("id").toString(),
                    content,
                    mediaList,
                    facebookPage
            );
            return PlatformPostResult.success(PlatformType.FACEBOOK_PAGE, post);
        } catch (Exception e) {
            return PlatformPostResult.failure(PlatformType.FACEBOOK_PAGE, "Failed to create post: " + e.getMessage());
        }
    }

    @Override
    public PlatformPostResult createFacebookReel(File videoFile, String title, String content, Date scheduledAt, Long pageId) {
        try {
            String filename = videoFile.getName();
            if (!filename.matches(".*\\.(mp4|avi|mov|mkv|webm|flv|wmv|mpeg|3gp)$")) {
                return PlatformPostResult.failure(PlatformType.FACEBOOK_PAGE, "Video file is required for Reel post.");
            }
            FacebookPageTokenDTO tokenDTO = fetchPageToken(pageId);
            RestTemplate restTemplate = new RestTemplate();

            System.out.println("ready to initiate video");
            String videoId = initiateVideo(tokenDTO);

            System.out.println("ready to upload video");
            // Step 2: Upload the video
            ResponseEntity<String> uploadResponse = uploadVideo(videoFile, tokenDTO, videoId);
            if (!uploadResponse.getStatusCode().is2xxSuccessful()) return PlatformPostResult.failure(PlatformType.FACEBOOK_PAGE, "Failed to upload video." + uploadResponse.getBody());

            System.out.println("ready to publish reel");
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

            System.out.println("ready to validate result");
            if (!reelResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create reel post: " + reelResponse);
            }

            System.out.println("Reel posted successfully");

            return PlatformPostResult.success(PlatformType.FACEBOOK_PAGE, "Reel posted successfully! ID: " + reelResponse.getBody());
        } catch (Exception e) {
            return PlatformPostResult.failure(PlatformType.FACEBOOK_PAGE, "Failed to post Reel: " + e.getMessage());
        }
    }

    @Override
    public String initVideo(Long facebookPageId) {
        FacebookPageTokenDTO tokenDTO = getPageCachedToken(facebookPageId);
        return initiateVideo(tokenDTO);
    }

    @Override
    public ResponseEntity<String> uploadVideoToFacebook(Long facebookPageId, MultipartFile videoFile, String videoId) {
        try{
            FacebookPageTokenDTO tokenDTO = getPageCachedToken(facebookPageId);
            File tempVideoFile = File.createTempFile("upload_" + videoId, ".tmp");
            videoFile.transferTo(tempVideoFile);
            ResponseEntity<String> response = uploadVideo(tempVideoFile, tokenDTO, videoId);
            boolean delete = tempVideoFile.delete();
            if (delete && response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Video uploaded successfully!");
            }
            return response;
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to upload video: " + e.getMessage());
        }
    }

    private String initiateVideo(FacebookPageTokenDTO tokenDTO){
        try {
            RestTemplate restTemplate = new RestTemplate();
            String uploadUrl = String.format("https://graph.facebook.com/v22.0/%s/videos", tokenDTO.facebookPageId());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("access_token", tokenDTO.accessToken());
            body.add("upload_phase", "start");

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.POST, request, Map.class);
            return uploadResponse.getBody().get("video_id").toString();
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

            ResponseEntity<String> uploadResponse = restTemplate.exchange(
                    String.format("https://rupload.facebook.com/video-upload/v22.0/%s", videoId),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // Optional: Delete file after upload
            videoFile.delete();
            return uploadResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload video: " + e.getMessage());
        }
    }



    @Override
    public PlatformPostResult storyOnFacebookPage(Long suitId, FileData image, String title, String content, Date scheduledAt, Long facebookPageId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> testRefreshMethod(Long accountId) {
        try{
            return refreshAllTokens(fetchAccountToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED));
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
    public String testPostingWithMediaIds(List<String> mediaIds, Long pageId){
        try {
            FacebookPageTokenDTO tokenDTO = fetchPageToken(pageId);
            RestTemplate restTemplate = new RestTemplate();

            String url = String.format("https://graph.facebook.com/v22.0/%s/feed", tokenDTO.facebookPageId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<Map<String, String>> attachedMedia = mediaIds.stream()
                    .map(id -> Map.of("media_fbid", id))
                    .toList();

            Map<String, Object> body = new HashMap<>();
            body.put("message", "bjeh rabi e5dim");
            body.put("attached_media", attachedMedia);
            body.put("access_token", tokenDTO.accessToken());  // Use the correct Page Access Token

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return "Post Created! ID: " + Objects.requireNonNull(response.getBody()).toString();
        } catch (Exception e) {
            return "Failed to create post: " + e.getMessage();
        }
    }

    @Transactional
    @Override
    public FacebookAccount exchangeCodeForToken(String authorizationCode, boolean isGlobal, String redirectUri) throws Exception {
        //get short-lived authentication token
        String getTokenUrl = String.format(
                "https://graph.facebook.com/v19.0/oauth/access_token?client_id=%s&client_secret=%s&redirect_uri=%s&code=%s",
                clientId, clientSecret, redirectUri, authorizationCode
        );

        RestTemplate authRestTemplate = new RestTemplate();
        ResponseEntity<String> authResponse = authRestTemplate.getForEntity(getTokenUrl, String.class);

        if (authResponse.getBody() == null) throw new RuntimeException("Facebook response is null.");

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode authJsonNode = objectMapper.readTree(authResponse.getBody());
        String shortLivedToken = authJsonNode.has("access_token") ? authJsonNode.get("access_token").asText() : null;

        if (shortLivedToken == null) throw new RuntimeException("Short-lived access token is missing. Full response: " + authResponse.getBody());

        //find or create new local facebook account
        JsonNode userInfo = fetchUserInfoFromFacebook(List.of("id", "name"), shortLivedToken);

        FacebookAccount savedAccount = facebookAccountRepository.findFacebookAccountByFacebookId(userInfo.get("id").asText())
                .orElseGet(() -> {
                    FacebookAccount newAccount = new FacebookAccount(
                            userInfo.get("name").asText(),
                            userInfo.get("id").asText(),
                            new Date(),
                            new Date(),
                            isGlobal
                    );
                    return facebookAccountRepository.save(newAccount);
                });

        //exchange short-lived token for long-lived token
        String url = String.format(
                "https://graph.facebook.com/v19.0/oauth/access_token" +
                        "?grant_type=fb_exchange_token" +
                        "&client_id=%s" +
                        "&client_secret=%s" +
                        "&fb_exchange_token=%s",
                clientId, clientSecret, shortLivedToken
        );

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        JsonNode jsonNode = objectMapper.readTree(response.getBody());

        String longLivedToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0;

        if (longLivedToken == null) throw new RuntimeException("Long-lived token is missing. Full response: " + jsonNode);

        FacebookAccountToken savedToken = generateAndSaveAccountToken(longLivedToken, expiresIn, savedAccount, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED);

        cacheAccountToken(savedToken);

        return savedAccount;
    }

    @Override
    public FacebookPage authenticateFacebookPage(Long accountId, String pageId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookAccountTokenDTO userToken = getAccountCachedToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED);

        String url = String.format("https://graph.facebook.com/v19.0/%s?fields=id,name,access_token&access_token=%s",
                pageId, userToken.accessToken());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch page details. Status: " + response.getStatusCode());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode= objectMapper.readTree(response.getBody());

        FacebookPage facebookPage = facebookPageRepository.findFacebookPageByFacebookId(jsonNode.get("id").asText())
                .orElseGet(()->{
                            FacebookPage newPage = new FacebookPage();
                            newPage.setFacebookAccount(getFacebookAccount(accountId));
                            newPage.setPageName(jsonNode.get("name").asText());
                            newPage.setFacebookPageId(jsonNode.get("id").asText());
                            newPage.setConnectedAt(new Date());
                            newPage.setUpdatedAt(new Date());
                            return facebookPageRepository.save(newPage);
                        }
                );
        String pageAccessToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0;

        if (pageAccessToken == null) throw new RuntimeException("Long-lived token is missing. Full response: " + jsonNode);

        FacebookPageToken newToken = generateAndSavePageToken(pageAccessToken,expiresIn, facebookPage);
        cachePageToken(newToken);

        return facebookPage;
    }

    @Override
    public FacebookAccountTokenDTO getAccountCachedToken(Long accountId, FacebookOAuthTokenType tokenType) {

        String key = formulateAccountKey(accountId, tokenType);
        List<FacebookAccountTokenDTO> tokenList = redisAccountTemplate.opsForList().range(key, 0, -1);


        if (tokenList == null || tokenList.isEmpty() || tokenList.stream().allMatch((token) -> token.facebookAccountId() == null)) {
            return fetchAccountToken(accountId, tokenType);
        }

        // Identify the token with the longest expiration
        FacebookAccountTokenDTO bestToken = tokenList.stream()
                .max(Comparator.comparingInt(token -> token.expiresIn() == 0 ? Integer.MAX_VALUE : token.expiresIn()))
                .orElseThrow(() -> new IllegalStateException("Failed to determine the best token for account: " + accountId));

        // If duplicates exist, remove all and keep only the best token
        if (tokenList.size() > 1) {
            redisAccountTemplate.delete(key);
            redisAccountTemplate.opsForList().rightPush(key, bestToken);
        }
        return bestToken;
    }

    @Override
    public FacebookPageTokenDTO getPageCachedToken(Long pageId) throws ResourceNotFoundException, IllegalStateException {

        String key = formulatePageKey(pageId);
        List<FacebookPageTokenDTO> tokenList = redisPageTemplate.opsForList().range(key, 0, -1);

        if (tokenList == null || tokenList.isEmpty()) {
            return fetchPageToken(pageId);
        }

        // Identify the token with the longest expiration
        FacebookPageTokenDTO bestToken = tokenList.stream()
                .max(Comparator.comparingInt(token -> token.expiresIn() == 0 ? Integer.MAX_VALUE : token.expiresIn()))
                .orElseThrow(() -> new IllegalStateException("Failed to determine the best token for page: " + pageId));
        // If duplicates exist, remove all and keep only the best token
        if (tokenList.size() > 1) {
            redisPageTemplate.delete(key);
            redisPageTemplate.opsForList().rightPush(key, bestToken);
        }
        return bestToken;
    }

    @Override
    public JsonNode fetchUserPages(Long accountId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookAccountTokenDTO token;

        token = getAccountCachedToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED);

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

    //Utility private methods -------------------------------------------------------------------------------------------------------------------------------

    private FacebookPageTokenDTO fetchPageToken(Long pageId) {
        List<FacebookPageToken> tokens = facebookOAuthTokenService.getTokenByPageId(pageId);
        FacebookPageToken token = tokens.stream()
                .filter(t -> validatePageToken(facebookPageTokenDTOMapper.apply(t)))
                .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for page: " + pageId + ", please authenticate again.");
        }
        return cachePageToken(token);
    }

    private ResponseEntity<Object> refreshAllTokens(FacebookAccountTokenDTO accountToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://graph.facebook.com/v22.0/oauth/access_token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "fb_exchange_token");
            body.add("client_id", clientId); // Replace with your App ID
            body.add("client_secret", clientSecret); // Replace with your App Secret
            body.add("fb_exchange_token", accountToken.accessToken());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody.containsKey("access_token")) {
                    String newAccessToken = responseBody.get("access_token").toString();

                    Integer expiresIn = responseBody.containsKey("expires_in")
                            ? Integer.parseInt(responseBody.get("expires_in").toString())
                            : 0; // Assume long-lived if not provided

                    FacebookAccount facebookAccount = getFacebookAccountByFacebookId(accountToken.facebookAccountId());
                    FacebookAccountToken newAccountToken = generateAndSaveAccountToken(newAccessToken, expiresIn, facebookAccount, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED);

                    FacebookAccountTokenDTO accountTokenDTO = cacheAccountToken(newAccountToken);

                    // Refresh page tokens
                    List<FacebookPageTokenDTO> pageTokenDTOS = refreshPageTokens(accountToken);

                    HashMap<String, Object> responseMap = new HashMap<>();
                    responseMap.put("accountToken", accountTokenDTO);
                    responseMap.put("pageTokens", pageTokenDTOS);

                    return ResponseEntity.ok(responseMap);
                } else {
                    throw new RuntimeException("Failed to refresh account token: No access token returned.");
                }
            } else {
                throw new RuntimeException("Failed to refresh account token: No data returned.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing Facebook account token: " + e.getMessage(), e);
        }
    }

    private void revokeAllAccountTokens(FacebookAccount account) {
        List<FacebookAccountToken> tokens = facebookOAuthTokenService.getTokensByAccountId(account.getId());
        for (FacebookAccountToken token : tokens) {
            token.setRevoked(true);
            facebookOAuthTokenService.saveToken(token);
        }
    }

    private void revokeAllPageTokens(FacebookPage page) {
        List<FacebookPageToken> tokens = facebookOAuthTokenService.getTokenByPageId(page.getId());
        for (FacebookPageToken token : tokens) {
            token.setRevoked(true);
            facebookOAuthTokenService.savePageToken(token);
        }
    }

    private List<FacebookPageTokenDTO> refreshPageTokens(FacebookAccountTokenDTO accountToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("https://graph.facebook.com/v22.0/me/accounts?access_token=%s", accountToken.accessToken());

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Map<String, Object>> pages = (List<Map<String, Object>>) response.getBody().get("data");

                List<FacebookPageTokenDTO> newPageTokens = new ArrayList<>();

                for (Map<String, Object> page : pages) {
                    try{
                        FacebookPage facebookPage = getPageByFacebookId(page.get("id").toString());
                        String pageAccessToken = page.get("access_token").toString();

                        Integer expiresIn = page.containsKey("expires_in")
                                ? Integer.parseInt(page.get("expires_in").toString())
                                : 0; // Assume long-lived if not provided

                        newPageTokens.add(facebookPageTokenDTOMapper.apply(
                                generateAndSavePageToken(pageAccessToken, expiresIn, facebookPage)
                        ));
                    }catch (ResourceNotFoundException e){
                        continue;
                    }
                }
                return newPageTokens;
            } else {
                throw new RuntimeException("Failed to refresh page tokens: No data returned.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing Facebook page tokens: " + e.getMessage(), e);
        }
    }

    private FacebookPage getPageByFacebookId(String facebookId) {
        return facebookPageRepository.findFacebookPageByFacebookId(facebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with Facebook ID: " + facebookId));
    }

    private FacebookPageToken generateAndSavePageToken(String pageToken, int expiresIn, FacebookPage facebookPage) {

        FacebookPageToken oAuthToken = new FacebookPageToken();
        String encryptedToken;
        try{
            encryptedToken= encryptionService.encrypt(pageToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token: " + e.getMessage());
        }
        oAuthToken.setAccessToken(encryptedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setPage(facebookPage);

        revokeAllPageTokens(facebookPage);

        return facebookOAuthTokenService.savePageToken(oAuthToken);
    }

    private FacebookAccountTokenDTO cacheAccountToken(@NotNull FacebookAccountToken token) {
        FacebookAccountTokenDTO tokenDTO = facebookAccountTokenDTOMapper.apply(token);
        String key = formulateAccountKey(token.getAccount().getId(), token.getOAuthTokenType());

        List<FacebookAccountTokenDTO> existingTokens = redisAccountTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisAccountTemplate.delete(key);
        }

        redisAccountTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }

    private FacebookPageTokenDTO cachePageToken(FacebookPageToken token) {
        FacebookPageTokenDTO tokenDTO = facebookPageTokenDTOMapper.apply(token);

        String key = formulatePageKey(token.getPage().getId());

        List<FacebookPageTokenDTO> existingTokens = redisPageTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisPageTemplate.delete(key);
        }

        redisPageTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }

    private FacebookAccountToken generateAndSaveAccountToken(String longLivedToken, int expiresIn, FacebookAccount facebookAccount, FacebookOAuthTokenType tokenType) {

        FacebookAccountToken oAuthToken = new FacebookAccountToken();
        String encryptedToken;
        try{
             encryptedToken= encryptionService.encrypt(longLivedToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token: " + e.getMessage());
        }
        oAuthToken.setAccessToken(encryptedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setAccount(facebookAccount);
        oAuthToken.setOAuthTokenType(tokenType);

        revokeAllAccountTokens(facebookAccount);

        return facebookOAuthTokenService.saveToken(oAuthToken);
    }

    private FacebookAccountTokenDTO fetchAccountToken(Long accountId, FacebookOAuthTokenType tokenType) throws ResourceNotFoundException {
        FacebookAccountToken token;
            token = facebookOAuthTokenService.getTokenByAccountIdAndType(accountId, tokenType)
                    .stream().filter(t -> validateAccountToken(facebookAccountTokenDTOMapper.apply(t)))
                    .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId +  ", please authenticate again.");
        }
        return cacheAccountToken(token);
    }

    private JsonNode fetchUserInfoFromFacebook(List<String> requestedFields, String token) throws Exception {
        String fields = String.join(",", requestedFields);
        String url = String.format("https://graph.facebook.com/v19.0/me?fields=%s&access_token=%s",fields, token);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    private FacebookAccount getFacebookAccount(Long accountId) {
        return facebookAccountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException(String.format("Account with id :%d not found", accountId)));
    }

    private FacebookAccount getFacebookAccountByFacebookId(String facebookId) {
        return facebookAccountRepository.findFacebookAccountByFacebookId(facebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with Facebook ID: " + facebookId));
    }

    private boolean validateAccountToken(FacebookAccountTokenDTO token) {
        if (token == null) return false;
        if (token.expiresIn() == 0) return true;
        long expirationMillis = TimeUnit.MILLISECONDS.convert(token.expiresIn(), token.expiresInTimeUnit());
        return System.currentTimeMillis() <= expirationMillis;
    }

    private boolean validatePageToken(FacebookPageTokenDTO token) {
        if (token == null) return false;
        if (token.expiresIn() == 0) return true;
        long expirationMillis = TimeUnit.MILLISECONDS.convert(token.expiresIn(), token.expiresInTimeUnit());
        return System.currentTimeMillis() <= expirationMillis;
    }

    private String formulateAccountKey(Long id, FacebookOAuthTokenType tokenType) {
        return tokenType.toString() + id.toString();
    }

    private String formulatePageKey(Long id) {
        return "pageId_"+ id.toString();
    }

}
