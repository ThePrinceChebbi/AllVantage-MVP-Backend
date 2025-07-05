package com.MarketingMVP.AllVantage.Services.Platform_Specific.LinkedIn;

import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Account.LinkedInAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Account.LinkedInAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken.LinkedinTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken.LinkedinTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.LinkedIn.Account.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.LinkedIn.Organization.LinkedInOrganization;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.LinkedIn.LinkedinToken;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.LinkedIn.LinkedInAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.LinkedIn.LinkedInOrganizationRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn.LinkedinMediaRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn.LinkedinPostRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn.LinkedinReelRepository;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Linkedin.LinkedinTokenRepository;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LinkedInServiceImpl implements LinkedInService{

    private final LinkedInOrganizationRepository linkedInOrganizationRepository;
    private final FileService fileService;
    private final LinkedinMediaRepository linkedinMediaRepository;
    private final LinkedinPostRepository linkedinPostRepository;
    private final LinkedinReelRepository linkedinReelRepository;

    @Value("${linkedin.client.id}")
    private String clientId;

    @Value("${linkedin.client.secret}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/api/v1/account/linkedin/callback";

    private final RedisTemplate<String, LinkedinTokenDTO> redisLinkedinTemplate;
    private final AESEncryptionService encryptionService;
    private final LinkedInAccountRepository linkedInAccountRepository;
    private final LinkedinTokenRepository linkedinTokenRepository;

    public LinkedInServiceImpl(RedisTemplate<String,LinkedinTokenDTO> redisLinkedinTemplate, AESEncryptionService encryptionService, LinkedInAccountRepository linkedInAccountRepository, LinkedinTokenRepository linkedinTokenRepository, LinkedInOrganizationRepository linkedInOrganizationRepository, FileService fileService, LinkedinMediaRepository linkedinMediaRepository, LinkedinPostRepository linkedinPostRepository, LinkedinReelRepository linkedinReelRepository) {
        this.redisLinkedinTemplate = redisLinkedinTemplate;
        this.encryptionService = encryptionService;
        this.linkedInAccountRepository = linkedInAccountRepository;
        this.linkedinTokenRepository = linkedinTokenRepository;
        this.linkedInOrganizationRepository = linkedInOrganizationRepository;
        this.fileService = fileService;
        this.linkedinMediaRepository = linkedinMediaRepository;
        this.linkedinPostRepository = linkedinPostRepository;
        this.linkedinReelRepository = linkedinReelRepository;
    }

    @Override
    public RedirectView getAuthenticationCode(String redirectUri) {

        final String scopes = "openid%20" +
                "profile%20" +
                "r_organization_social%20" +
                "rw_organization_admin%20" +
                "w_member_social%20" +
                "w_organization_social%20" +
                "r_events%20" +
                "r_organization_admin%20" +
                "email%20" +
                "r_ads%20" +
                "r_1st_connections_size";
//                "rw_events%20" +
//                "r_ads_reporting%20" +
//                "rw_ads%20" +


        String authUrl = "https://www.linkedin.com/oauth/v2/authorization" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope="+scopes;

        return new RedirectView(authUrl);
    }

    @Override
    public RedirectView authenticateGlobalAccount() {
        return getAuthenticationCode(redirectUri);
    }

    @Transactional
    @Override
    public RedirectView authenticateGlobalAccountCallback(String authorizationCode) {
        try{
            LinkedInAccountDTO account = new LinkedInAccountDTOMapper()
                    .apply(exchangeCodeForToken(authorizationCode, redirectUri));
            String redirectUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:4200/")
                    .queryParam("name", account.accountName())
                    .queryParam("platform", "Linkedin")
                    .queryParam("message", "success")
                    .build()
                    .toUriString();

            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            String redirectUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:4200/")
                    .queryParam("platform", "Facebook")
                    .queryParam("message", "failed")
                    .build()
                    .toUriString();

            return new RedirectView(redirectUrl);
        }
    }

    @Transactional
    public LinkedInAccount exchangeCodeForToken(String authorizationCode, String redirectUri) throws Exception {
        String getTokenUrl = String.format(
                "https://www.linkedin.com/oauth/v2/accessToken" +
                        "?grant_type=authorization_code" +
                        "&code=%s" +
                        "&redirect_uri=%s" +
                        "&client_id=%s" +
                        "&client_secret=%s",
                authorizationCode, redirectUri, clientId, clientSecret
        );

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(getTokenUrl, null, String.class);

        if (tokenResponse.getBody() == null)
            throw new RuntimeException("LinkedIn response is null.");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenJson = mapper.readTree(tokenResponse.getBody());

        String accessToken = tokenJson.has("access_token") ? tokenJson.get("access_token").asText() : null;

        if (accessToken == null)
            throw new RuntimeException("Access token missing. Full response: " + tokenResponse.getBody());

        // Fetch profile info
        JsonNode userInfo = fetchUserInfoFromLinkedIn(accessToken);
        System.out.println(userInfo);
        System.out.println(userInfo.get("sub").asText());
        System.out.println(userInfo.get("name").asText());
        LinkedInAccount account = linkedInAccountRepository.findByLinkedinId(userInfo.get("sub").asText())
                .orElseGet(() -> linkedInAccountRepository.save(new LinkedInAccount(
                        userInfo.get("sub").asText(),
                        userInfo.get("name").asText(),
                        new Date(),
                        new Date()
                )));

        System.out.println(tokenJson);
        System.out.println(tokenJson.get("expires_in").asInt());
        int expiresIn = tokenJson.has("expires_in") ? tokenJson.get("expires_in").asInt() : 60 * 60 * 60; // fallback to 60 hours
        LinkedinToken savedToken = generateAndSaveToken(accessToken, expiresIn, account);
        cacheToken(savedToken);

        return account;
    }

    private JsonNode fetchUserInfoFromLinkedIn(String accessToken) throws Exception {
        String url = "https://api.linkedin.com/v2/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getBody() == null)
            throw new RuntimeException("LinkedIn profile fetch failed.");

        return new ObjectMapper().readTree(response.getBody());
    }

    private LinkedinTokenDTO cacheToken(LinkedinToken token) {
        LinkedinTokenDTO tokenDTO = new LinkedinTokenDTOMapper(encryptionService).apply(token);

        String key = formulateKey(token.getAccount().getId());

        List<LinkedinTokenDTO> existingTokens = redisLinkedinTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisLinkedinTemplate.delete(key);
        }

        redisLinkedinTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }

    public String formulateKey(Long id) {
        return "linkedinId_"+ id.toString();
    }

    private LinkedinToken generateAndSaveToken(String longLivedToken, int expiresIn, LinkedInAccount linkedInAccount) {

        LinkedinToken oAuthToken = new LinkedinToken();
        String encryptedToken;
        try{
            encryptedToken= encryptionService.encrypt(longLivedToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token: " + e.getMessage());
        }
        oAuthToken.setAccessToken(encryptedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setAccount(linkedInAccount);

        revokeAllTokens(linkedInAccount);

        return saveToken(oAuthToken);
    }

    private void revokeAllTokens(LinkedInAccount account) {
        List<LinkedinToken> tokens = getTokensByAccountId(account.getId());
        for (LinkedinToken token : tokens) {
            token.setRevoked(true);
            saveToken(token);
        }
    }

    public List<LinkedinToken> getTokensByAccountId(Long accountId) {
        return linkedinTokenRepository.findByAccountId(accountId);
    }

    public LinkedinToken saveToken(LinkedinToken token) {
        return linkedinTokenRepository.save(token);
    }

    @Override
    public JsonNode fetchAdministeredPages(Long accountId) throws Exception {

        LinkedinTokenDTO tokenDTO = getCachedToken(accountId);

        String url = "https://api.linkedin.com/v2/organizationAcls" +
                "?q=roleAssignee&role=ADMINISTRATOR&state=APPROVED" +
                "&projection=(elements*(*,organization~(localizedName,vanityName)))";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenDTO.accessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getBody() == null) throw new RuntimeException("Failed to fetch LinkedIn Pages");

        return new ObjectMapper().readTree(response.getBody());
    }

    public LinkedinTokenDTO getCachedToken(Long pageId) throws ResourceNotFoundException, IllegalStateException {

        String key = formulateKey(pageId);
        List<LinkedinTokenDTO> tokenList = redisLinkedinTemplate.opsForList().range(key, 0, -1);

        if (tokenList == null || tokenList.isEmpty()) {
            return fetchToken(pageId);
        }

        // Identify the token with the longest expiration
        LinkedinTokenDTO bestToken = tokenList.stream()
                .max(Comparator.comparingInt(token -> token.expiresIn() == 0 ? Integer.MAX_VALUE : token.expiresIn()))
                .orElseThrow(() -> new IllegalStateException("Failed to determine the best token for page: " + pageId));
        if (tokenList.size() > 1) {
            redisLinkedinTemplate.delete(key);
            redisLinkedinTemplate.opsForList().rightPush(key, bestToken);
        }
        return bestToken;
    }

    public LinkedinTokenDTO fetchToken(Long accountId){
        List<LinkedinToken> tokens = linkedinTokenRepository.findByAccountId(accountId);
        LinkedinToken token = tokens.stream()
                .filter(t -> !t.isRevoked())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid token found for account: " + accountId));

/*        LinkedinToken token = tokens.stream()
                .filter(this::validateToken)
                .findFirst().orElse(null);*/
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId + ", please authenticate again.");
        }
        return cacheToken(token);
    }

    @Override
    public LinkedInOrganization authenticateLinkedInOrganization(Long accountId, String organizationId) throws Exception {
        LinkedinTokenDTO tokenDTO = getCachedToken(accountId); // Reuse your method

        if (linkedInOrganizationRepository.findByOrganizationId(organizationId).isPresent()) {
            throw new IllegalArgumentException(String.format("LinkedIn organization with linkedin id: %s already exists.",organizationId));
        }

        LinkedInAccount account = linkedInAccountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("LinkedIn account not found with ID: " + accountId)
        );

        String url = "https://api.linkedin.com/v2/organizations/" + organizationId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenDTO.accessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch organization details. Status: " + response.getStatusCode());
        }

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());

        String name = jsonNode.path("localizedName").asText();
        String username = jsonNode.path("vanityName").asText();

        LinkedInOrganization linkedInOrg = linkedInOrganizationRepository
                .findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    LinkedInOrganization newOrg = new LinkedInOrganization();
                    newOrg.setLinkedInAccount(account);
                    newOrg.setPageName(name);
                    newOrg.setOrganizationId(organizationId);
                    newOrg.setOrganizationUsername(username);
                    newOrg.setConnectedAt(new Date());
                    newOrg.setUpdatedAt(new Date());
                    return linkedInOrganizationRepository.save(newOrg);
                });

        linkedInOrg.setUpdatedAt(new Date());
        return linkedInOrganizationRepository.save(linkedInOrg);
    }

    public PlatformPostResult createLinkedInPost(List<FileData> files, String content, LinkedInOrganization organization, boolean isReel) {
        try {
            // Validate that all files are either images or a single video
            boolean hasVideo = files.stream().anyMatch(file -> "video".equals(file.getType()));
            boolean hasImage = files.stream().anyMatch(file -> "image".equals(file.getType()));

            if (hasVideo && hasImage) {
                throw new IllegalArgumentException("Cannot create a post with both images and videos.");
            }
            if (hasVideo && files.size() > 1) {
                throw new IllegalArgumentException("LinkedIn only supports one video per post.");
            }
            if (files.size() > 20) {
                throw new IllegalArgumentException("LinkedIn supports up to 9 images per post.");
            }

            // Get the cached token
            LinkedinTokenDTO tokenDTO = getCachedToken(organization.getLinkedInAccount().getId());
            String accessToken = tokenDTO.accessToken();

            System.out.println("Access token: " + accessToken);

            // First upload any media files and collect their media IDs
            List<LinkedinMedia> uploadedMedia = new ArrayList<>();

            for (FileData file : files) {
                String fileType = file.getType();

                if ("video".equals(fileType) || "image".equals(fileType)) {
                    byte[] fileBytes = fileService.getFileBytesByFileData(file);
                    Long fileSize = "video".equals(fileType) ? (long) fileBytes.length : null;

                    // Upload registration
                    JsonNode uploadRegistrationResult = initializeUpload(
                            "urn:li:organization:" + organization.getOrganizationId(),
                            accessToken,
                            "video".equals(fileType),
                            fileSize
                    );

                    JsonNode valueNode = uploadRegistrationResult.get("value");
                    String uploadUrl = "video".equals(fileType) ?
                            valueNode.get("uploadInstructions").get(0).get("uploadUrl").asText() :
                            valueNode.get("uploadUrl").asText();

                    String mediaUrn = "video".equals(fileType) ?
                            valueNode.get("video").asText() :
                            valueNode.get("image").asText();

                    String uploadToken = "video".equals(fileType) ?
                            valueNode.get("uploadToken").asText() : null;

                    LinkedinMedia media = "video".equals(fileType)
                            ? uploadVideoToLinkedIn(uploadUrl, file, mediaUrn, uploadToken, tokenDTO.accessToken())
                            : uploadImageToLinkedIn(uploadUrl, file, mediaUrn);

                    uploadedMedia.add(media);
                } else {
                    throw new IllegalArgumentException("Unsupported file type: " + fileType);
                }
            }

            // Now create the post with any uploaded media
            String url = "https://api.linkedin.com/rest/posts";
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode postRequest = mapper.createObjectNode();

            // Set author as the organization
            postRequest.put("author", "urn:li:organization:" + organization.getOrganizationId());
            postRequest.put("commentary", content);
            postRequest.put("visibility", "PUBLIC");

            // Set distribution details with empty arrays for targetEntities and thirdPartyDistributionChannels
            ObjectNode distribution = postRequest.putObject("distribution");
            distribution.put("feedDistribution", "MAIN_FEED");
            distribution.putArray("targetEntities");
            distribution.putArray("thirdPartyDistributionChannels");

            postRequest.put("lifecycleState", "PUBLISHED");
            postRequest.put("isReshareDisabledByAuthor", false);

            // Set content with media
            if (!uploadedMedia.isEmpty()) {
                ObjectNode contentNode = postRequest.putObject("content");

                if ("image".equals(files.get(0).getType()) && uploadedMedia.size() > 1) {
                    // Correct LinkedIn format for multiple images
                    ObjectNode multiImageNode = contentNode.putObject("multiImage");
                    ArrayNode imageArray = multiImageNode.putArray("images");

                    for (LinkedinMedia media : uploadedMedia) {
                        ObjectNode imageObject = imageArray.addObject();
                        imageObject.put("id", media.getMediaId());
                    }
                } else {
                    // For single video or image
                    ObjectNode mediaNode = contentNode.putObject("media");
                    mediaNode.put("id", uploadedMedia.get(0).getMediaId());
                }
            }

            // Prepare and send request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("LinkedIn-Version", "202504");

            HttpEntity<String> request = new HttpEntity<>(postRequest.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String postUrn = response.getHeaders().getFirst("x-linkedin-id");

                if (isReel) {
                    LinkedinReel linkedinReel = new LinkedinReel();
                    linkedinReel.setLinkedinReelId(postUrn);
                    linkedinReel.setCaption(content);
                    linkedinReel.setLinkedinMedia(uploadedMedia.get(0));
                    linkedinReel.setOrganization(organization);
                    return PlatformPostResult.success(PlatformType.LINKEDIN, linkedinReelRepository.save(linkedinReel));
                }else {
                    LinkedinPost linkedinPost = new LinkedinPost();
                    linkedinPost.setLinkedinPostId(postUrn);
                    linkedinPost.setCaption(content);
                    linkedinPost.setLinkedinMediaList(uploadedMedia);
                    linkedinPost.setOrganization(organization);
                    return PlatformPostResult.success(PlatformType.LINKEDIN, linkedinPostRepository.save(linkedinPost));
                }

            } else {
                throw new RuntimeException("Failed to create LinkedIn post: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return PlatformPostResult.failure(PlatformType.LINKEDIN, e.getMessage());
        }
    }

    public JsonNode initializeUpload(String userUrn, String accessToken, boolean isVideo, Long fileSize) throws Exception {
        if (isVideo && (fileSize == null || fileSize <= 0)) {
            throw new IllegalArgumentException("File size must be positive for video uploads");
        }

        String url = isVideo
                ? "https://api.linkedin.com/rest/videos?action=initializeUpload"
                : "https://api.linkedin.com/rest/images?action=initializeUpload";

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> inner = new HashMap<>();
        inner.put("owner", userUrn);

        if (isVideo) {
            inner.put("fileSizeBytes", fileSize);
            inner.put("uploadCaptions", false);
            inner.put("uploadThumbnail", false);
        }

        requestBody.put("initializeUploadRequest", inner);

        String jsonPayload = mapper.writeValueAsString(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("LinkedIn-Version", "202504");
        headers.add("X-Restli-Protocol-Version", "2.0.0");

        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("LinkedIn initialize upload failed: " + response.getBody());
        }

        return mapper.readTree(response.getBody());
    }

    public LinkedinMedia uploadVideoToLinkedIn(
            String uploadUrl,
            FileData fileData,
            String mediaUrn,
            String uploadToken,
            String accessToken
    ) throws IOException, InterruptedException {

        // 1) PUSH THE BYTES & CAPTURE ETAG
        File file = fileService.getFileFromFileData(fileData);
        long fileSize = file.length();

        HttpURLConnection putConn = (HttpURLConnection) new URL(uploadUrl).openConnection();
        putConn.setDoOutput(true);
        putConn.setRequestMethod("PUT");
        putConn.setRequestProperty("Content-Type", "video/mp4");
        putConn.setRequestProperty("Content-Length", String.valueOf(fileSize));
        putConn.setConnectTimeout(30000);
        putConn.setReadTimeout(60000);

        try (OutputStream out = putConn.getOutputStream();
             FileInputStream in = new FileInputStream(file)) {
                byte[] buf = new byte[16 * 1024];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
                out.flush();
            }

        int code = putConn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new RuntimeException("Video upload failed: " + code + " " + putConn.getResponseMessage());
        }
        String etag = putConn.getHeaderField("ETag");

        // 2) FINALIZE UPLOAD
        String finalizeUrl = "https://api.linkedin.com/rest/videos?action=finalizeUpload";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode finalizeReq = mapper.createObjectNode();
        ObjectNode body = finalizeReq.putObject("finalizeUploadRequest");
        body.put("video", mediaUrn);
        body.put("uploadToken", uploadToken);
        ArrayNode parts = body.putArray("uploadedPartIds");
        parts.add(etag);

        HttpURLConnection finalConn = (HttpURLConnection) new URL(finalizeUrl).openConnection();
        finalConn.setRequestMethod("POST");
        finalConn.setDoOutput(true);
        finalConn.setRequestProperty("Authorization", "Bearer " + accessToken);
        finalConn.setRequestProperty("LinkedIn-Version", "202504");
        finalConn.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");
        finalConn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = finalConn.getOutputStream()) {
            os.write(mapper.writeValueAsBytes(finalizeReq));
        }
        if (finalConn.getResponseCode() / 100 != 2) {
            throw new RuntimeException("Finalize upload failed: " +
                    finalConn.getResponseCode() + " " + finalConn.getResponseMessage());
        }

        // 3) POLL UNTIL READY
        String encodedUrn = URLEncoder.encode(mediaUrn, StandardCharsets.UTF_8);
        String statusUrl = "https://api.linkedin.com/rest/videos/" + encodedUrn;
        long start = System.currentTimeMillis(), timeout = TimeUnit.MINUTES.toMillis(2);

        while (true) {
            if (System.currentTimeMillis() - start > timeout) {
                throw new RuntimeException("Video processing timed out for URN: " + mediaUrn);
            }

            HttpURLConnection statusConn = (HttpURLConnection) new URL(statusUrl).openConnection();
            statusConn.setRequestMethod("GET");
            statusConn.setRequestProperty("Authorization", "Bearer " + accessToken);
            statusConn.setRequestProperty("LinkedIn-Version", "202504");
            statusConn.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");
            statusConn.setConnectTimeout(5000);
            statusConn.setReadTimeout(5000);

            if (statusConn.getResponseCode() / 100 == 2) {
                JsonNode root = mapper.readTree(statusConn.getInputStream());
                String state = root.path("status").asText();
                System.out.println("Current video.status = " + state);
                if ("AVAILABLE".equalsIgnoreCase(state)) break;
                if ("PROCESSING_FAILED".equalsIgnoreCase(state)) {
                    throw new RuntimeException("LinkedIn video processing failed for URN: " + mediaUrn);
                }
            }

            Thread.sleep(5000);
        }

        // 4) SAVE & RETURN
        LinkedinMedia lm = new LinkedinMedia();
        lm.setMediaId(mediaUrn);
        lm.setFile(fileData);
        lm.setMediaType(PlatformMediaType.VIDEO);
        return linkedinMediaRepository.save(lm);
    }

    public LinkedinMedia uploadImageToLinkedIn(String uploadUrl, FileData fileData, String mediaUrn) throws IOException {
        System.out.println("Upload preparing");
        MediaType fileType;
        switch (fileData.getPrefix().substring(1)) {
            case "jpeg", "jpg" -> fileType = MediaType.IMAGE_JPEG;
            case "png" -> fileType = MediaType.IMAGE_PNG;
            default -> throw new IllegalArgumentException("Unsupported image type: " + fileData.getPrefix());
        }
        System.out.println(fileType);

        byte[] fileBytes = fileService.getFileBytesByFileData(fileData);

        System.out.println("File bytes: " + fileBytes.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(fileType);
        headers.setContentLength(fileBytes.length);
        headers.add("LinkedIn-Version", "202504");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.PUT, requestEntity, String.class
        );

        System.out.println("Upload completed: " + response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Image upload failed: " + response.getStatusCode() + " - " + response.getBody());
        }

        LinkedinMedia media = new LinkedinMedia();
        media.setMediaId(mediaUrn);
        media.setFile(fileData);
        media.setMediaType(PlatformMediaType.IMAGE);

        return linkedinMediaRepository.save(media);
    }
    @Override
    public PlatformInsightsResult getLinkedInInsights(Long organizationId, String postUrn) {
        try {
            LinkedInOrganization organization = linkedInOrganizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("LinkedIn organization not found with ID: " + organizationId));

            LinkedinTokenDTO tokenDTO = getCachedToken(organization.getLinkedInAccount().getId());
            String orgUrn = "urn:li:organization:" + organization.getOrganizationId();

            StringBuilder urlBuilder = new StringBuilder("https://api.linkedin.com/rest/organizationalEntityShareStatistics")
                    .append("?q=organizationalEntity")
                    .append("&organizationalEntity=").append(URLEncoder.encode(orgUrn, StandardCharsets.UTF_8));

            if (postUrn != null && !postUrn.isBlank()) {
                if (!postUrn.startsWith("urn:li:")) {
                    postUrn = postUrn.matches("\\d+") ? "urn:li:share:" + postUrn : postUrn;
                }

                String paramName = postUrn.contains("ugcPost") ? "ugcPosts" : "shares";
                urlBuilder.append("&").append(paramName).append("=List(")
                        .append(URLEncoder.encode(postUrn, StandardCharsets.UTF_8)).append(")");
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + tokenDTO.accessToken());
            conn.setRequestProperty("LinkedIn-Version", "202504");
            conn.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");

            int status = conn.getResponseCode();
            InputStream stream = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
            String responseStr = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining("\n"));

            if (status != 200) {
                return PlatformInsightsResult.failure(PlatformType.LINKEDIN, "LinkedIn API error (" + status + "): " + responseStr);
            }

            JsonNode response = new ObjectMapper().readTree(responseStr);
            JsonNode element = response.path("elements").isEmpty() ? null : response.path("elements").get(0);
            if (element == null) {
                return PlatformInsightsResult.failure(PlatformType.LINKEDIN, "No insight data found.");
            }

            Map<String, Object> insights = new HashMap<>();
            JsonNode statsNode = element.path("totalShareStatistics");
            statsNode.fields().forEachRemaining(entry -> {
                JsonNode val = entry.getValue();
                if (val.isNumber()) insights.put(entry.getKey(), val.numberValue());
                else if (val.isTextual()) insights.put(entry.getKey(), val.asText());
                else if (val.isBoolean()) insights.put(entry.getKey(), val.booleanValue());
            });

            if (element.has("share")) insights.put("shareUrn", element.get("share").asText());
            if (element.has("ugcPost")) insights.put("ugcPostUrn", element.get("ugcPost").asText());
            if (element.has("organizationalEntity")) insights.put("orgUrn", element.get("organizationalEntity").asText());

            return PlatformInsightsResult.success(PlatformType.LINKEDIN, insights);

        } catch (Exception e) {
            return PlatformInsightsResult.failure(PlatformType.LINKEDIN, "Error fetching insights: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getLinkedInOrgLogo400x400(Long accountId, String orgId) {
        try {
            LinkedinTokenDTO tokenDTO = getCachedToken(accountId);
            String url = String.format(
                    "https://api.linkedin.com/v2/organizations/%s?projection=(logoV2(original~:playableStreams))",
                    orgId
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + tokenDTO.accessToken());
            conn.setRequestProperty("LinkedIn-Version", "202504");
            conn.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");

            int status = conn.getResponseCode();
            if (status != 200) {
                String error = new BufferedReader(new InputStreamReader(conn.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                return ResponseEntity.status(status).body(Map.of("error", error));
            }

            String responseStr = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(responseStr);

            JsonNode elements = json
                    .path("logoV2")
                    .path("original~")
                    .path("elements");

            if (elements.isArray()) {
                for (JsonNode element : elements) {
                    String artifact = element.path("artifact").asText("");
                    if (artifact.contains("company-logo_400_400")) {
                        JsonNode identifiers = element.path("identifiers");
                        if (identifiers.isArray() && identifiers.size() > 0) {
                            String imageUrl = identifiers.get(0).path("identifier").asText("");
                            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
                        }
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No 400x400 image found"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Object> getAccountProfilePicture(Long accountId) {
        try {
            LinkedInAccount account = linkedInAccountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("LinkedIn account not found with ID: " + accountId));

            LinkedinTokenDTO tokenDTO = getCachedToken(account.getId());
            String url = "https://api.linkedin.com/v2/userinfo";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + tokenDTO.accessToken());
            conn.setRequestProperty("LinkedIn-Version", "202504");
            conn.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");

            int status = conn.getResponseCode();
            if (status != 200) {
                String error = new BufferedReader(new InputStreamReader(conn.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                return ResponseEntity.status(status).body(Map.of("error", error));
            }

            String response = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String pictureUrl = root.path("picture").asText();

            return ResponseEntity.ok(Map.of("imageUrl", pictureUrl));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public String deleteLinkedInPost(LinkedinPost linkedinPost) {
        try {
            boolean result = sendDeleteRequest(linkedinPost.getLinkedinPostId(), linkedinPost.getOrganization().getLinkedInAccount().getId());
            if (!result) {
                return "Failed to delete post. Please check the post URN and try again.";
            }
            linkedinPostRepository.delete(linkedinPost);
            linkedinMediaRepository.deleteAll(linkedinPost.getLinkedinMediaList());
            return "Post deleted successfully.";

        } catch (Exception e) {
            return "Error deleting post: " + e.getMessage();
        }
    }

    @Override
    public String deleteLinkedInReel(LinkedinReel linkedinReel) {
        try {
            boolean result = sendDeleteRequest(linkedinReel.getLinkedinReelId(), linkedinReel.getOrganization().getLinkedInAccount().getId());
            if (!result) {
                return "Failed to delete post. Please check the post URN and try again.";
            }
            linkedinReelRepository.delete(linkedinReel);
            linkedinMediaRepository.delete(linkedinReel.getLinkedinMedia());
            return "Post deleted successfully.";
        } catch (Exception e) {
            return "Error deleting post: " + e.getMessage();
        }
    }


    private boolean sendDeleteRequest(String postId, Long accountId) throws Exception {
        LinkedinTokenDTO tokenDTO = getCachedToken(accountId);

        String endpoint;
        if (postId.startsWith("urn:li:ugcPost:")) {
            endpoint = "https://api.linkedin.com/rest/posts/" + URLEncoder.encode(postId, StandardCharsets.UTF_8);
        } else if (postId.startsWith("urn:li:share:")) {
            endpoint = "https://api.linkedin.com/rest/shares/" + URLEncoder.encode(postId, StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Unsupported LinkedIn post ID format: " + postId);
        }

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + tokenDTO.accessToken());
        conn.setRequestProperty("LinkedIn-Version", "202504");
        conn.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");
        conn.setRequestProperty("X-RestLi-Method", "DELETE");

        int responseCode = conn.getResponseCode();
        if (responseCode == 204) {
            return true;
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String errorResponse = in.lines().collect(Collectors.joining());
            in.close();
            throw new RuntimeException("LinkedIn delete failed: " + errorResponse);
        }
    }

    @Override
    public ResponseEntity<Object> getAllLinkedInAccounts() {
        try {
            List<LinkedInAccount> accounts = linkedInAccountRepository.findAll();
            if (accounts.isEmpty()) {
                return ResponseEntity.status(404).body("No LinkedIn accounts found.");
            }
            return ResponseEntity.ok(accounts.stream().map(new LinkedInAccountDTOMapper()).toList());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
