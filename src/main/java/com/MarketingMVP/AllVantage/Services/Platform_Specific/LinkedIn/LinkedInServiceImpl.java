package com.MarketingMVP.AllVantage.Services.Platform_Specific.LinkedIn;

import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Account.LinkedInAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Account.LinkedInAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken.LinkedinTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken.LinkedinTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Account.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.LinkedIn.LinkedinToken;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.LinkedIn.LinkedInAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.LinkedIn.LinkedInOrganizationRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn.LinkedinMediaRepository;
import com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn.LinkedinPostRepository;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class LinkedInServiceImpl implements LinkedInService{

    private final LinkedInOrganizationRepository linkedInOrganizationRepository;
    private final FileService fileService;
    private final LinkedinMediaRepository linkedinMediaRepository;
    private final LinkedinPostRepository linkedinPostRepository;
    @Value("${linkedin.client.id}")
    private String clientId;

    @Value("${linkedin.client.secret}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/api/v1/account/linkedin/callback";

    private final RedisTemplate<String, LinkedinTokenDTO> redisLinkedinTemplate;
    private final AESEncryptionService encryptionService;
    private final LinkedInAccountRepository linkedInAccountRepository;
    private final LinkedinTokenRepository linkedinTokenRepository;

    public LinkedInServiceImpl(RedisTemplate<String,LinkedinTokenDTO> redisLinkedinTemplate, AESEncryptionService encryptionService, LinkedInAccountRepository linkedInAccountRepository, LinkedinTokenRepository linkedinTokenRepository, LinkedInOrganizationRepository linkedInOrganizationRepository, FileService fileService, LinkedinMediaRepository linkedinMediaRepository, LinkedinPostRepository linkedinPostRepository) {
        this.redisLinkedinTemplate = redisLinkedinTemplate;
        this.encryptionService = encryptionService;
        this.linkedInAccountRepository = linkedInAccountRepository;
        this.linkedinTokenRepository = linkedinTokenRepository;
        this.linkedInOrganizationRepository = linkedInOrganizationRepository;
        this.fileService = fileService;
        this.linkedinMediaRepository = linkedinMediaRepository;
        this.linkedinPostRepository = linkedinPostRepository;
    }

    @Override
    public RedirectView getAuthenticationCode(String redirectUri) {

        final String scopes = "openid%20" +
                "profile%20" +
                "r_organization_social%20" +
                "rw_organization_admin%20" +
                "w_member_social%20" +
                "w_organization_social%20" +
                "r_basicprofile%20" +
                "r_events%20" +
                "r_organization_admin%20" +
                "email%20" +
                "r_1st_connections_size";
//                "rw_events%20" +
//                "r_ads%20" +
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
    public ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode) {
        try {
            LinkedInAccountDTO account = new LinkedInAccountDTOMapper()
                    .apply(exchangeCodeForToken(authorizationCode, redirectUri));
            return ResponseEntity.ok(account);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
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

        LinkedinTokenDTO tokenDTO = getPageCachedToken(accountId);

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

    public LinkedinTokenDTO getPageCachedToken(Long pageId) throws ResourceNotFoundException, IllegalStateException {

        String key = formulateKey(pageId);
        List<LinkedinTokenDTO> tokenList = redisLinkedinTemplate.opsForList().range(key, 0, -1);

        if (tokenList == null || tokenList.isEmpty()) {
            return fetchPageToken(pageId);
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

    public LinkedinTokenDTO fetchPageToken(Long accountId){
        List<LinkedinToken> tokens = linkedinTokenRepository.findByAccountId(accountId);
        LinkedinToken token = tokens.stream()
                .filter(this::validateToken)
                .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId + ", please authenticate again.");
        }
        return cacheToken(token);
    }

    private boolean validateToken(LinkedinToken token) {
        if (token == null) return false;

        long expiresIn;
        TimeUnit timeUnit;
        expiresIn = token.getExpiresIn();
        timeUnit = TimeUnit.SECONDS;

        if (expiresIn == 0) return true;
        long expirationMillis = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(expiresIn, timeUnit);
        return System.currentTimeMillis() <= expirationMillis;

    }

    @Override
    public LinkedInOrganization authenticateLinkedInOrganization(Long accountId, String organizationId) throws Exception {
        LinkedinTokenDTO tokenDTO = getPageCachedToken(accountId); // Reuse your method
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

        LinkedInOrganization linkedInOrg = linkedInOrganizationRepository
                .findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    LinkedInOrganization newOrg = new LinkedInOrganization();
                    newOrg.setLinkedInAccount(account);
                    newOrg.setPageName(name);
                    newOrg.setOrganizationId(organizationId);
                    newOrg.setConnectedAt(new Date());
                    newOrg.setUpdatedAt(new Date());
                    return linkedInOrganizationRepository.save(newOrg);
                });

        // Update timestamp on every access
        linkedInOrg.setUpdatedAt(new Date());
        return linkedInOrganizationRepository.save(linkedInOrg);
    }

    public PlatformPostResult createLinkedInPost(List<FileData> files, String content, Date scheduledAt, Long organizationId) {
        try {
            // Get the LinkedIn organization
            LinkedInOrganization organization = linkedInOrganizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("LinkedIn organization not found with ID: " + organizationId));

            // Get the cached token
            LinkedinTokenDTO tokenDTO = getPageCachedToken(organization.getLinkedInAccount().getId());
            String accessToken = tokenDTO.accessToken();

            // First upload any media files and collect their media IDs
            List<LinkedinMedia> uploadedMedia = new ArrayList<>();

            for (FileData file : files) {
                LinkedinMedia media;
                String fileType = file.getType();

                System.out.println(fileType.substring(0, 1).toUpperCase() + fileType.substring(1) + " file detected: ");

                if ("video".equals(fileType) || "image".equals(fileType)) {

                    byte[] fileBytes = fileService.getFileBytesByFileData(file);
                    Long fileSize = "video".equals(fileType) ? (long) fileBytes.length : null;

                    if (fileSize != null) {
                        System.out.println("File size: " + fileSize);
                    }

                    JsonNode uploadRegistrationResult = initializeUpload(
                            "urn:li:organization:" + organization.getOrganizationId(),
                            accessToken,
                            "video".equals(fileType),
                            fileSize
                    );

                    System.out.println("Upload registration result: " + uploadRegistrationResult);

                    JsonNode valueNode = uploadRegistrationResult.get("value");
                    String uploadUrl = valueNode.get("uploadUrl").asText();
                    String mediaUrn = "video".equals(fileType) ? valueNode.get("video").asText() : valueNode.get("image").asText();


                    System.out.println("Upload URL: " + uploadUrl);

                    media = "video".equals(fileType)
                            ? uploadVideoToLinkedIn(uploadUrl, file, mediaUrn)
                            : uploadImageToLinkedIn(uploadUrl, file, mediaUrn);

                    System.out.println("Media ID: " + media.getMediaId());

                    uploadedMedia.add(media);
                } else {
                    throw new IllegalArgumentException("Unsupported file type: " + fileType);
                }
            }

            System.out.println("Done uploading media files.");
            // Now create the post with any uploaded media
            String url = "https://api.linkedin.com/rest/posts";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode postRequest = mapper.createObjectNode();

            // Set author as the organization
            postRequest.put("author", "urn:li:organization:" + organization.getOrganizationId());

            // Set commentary (formerly shareCommentary.text)
            postRequest.put("commentary", content);

            // Set visibility directly as a string
            postRequest.put("visibility", "PUBLIC");

            // Set distribution details with empty arrays for targetEntities and thirdPartyDistributionChannels
            ObjectNode distribution = postRequest.putObject("distribution");
            distribution.put("feedDistribution", "MAIN_FEED");
            distribution.putArray("targetEntities");
            distribution.putArray("thirdPartyDistributionChannels");

            // Set lifecycle state directly as a string
            postRequest.put("lifecycleState", scheduledAt != null ? "SCHEDULED" : "PUBLISHED");

            // Set reshare option
            postRequest.put("isReshareDisabledByAuthor", false);

            // Set content with media
            if (!uploadedMedia.isEmpty()) {
                ObjectNode contentNode = postRequest.putObject("content");
                ObjectNode mediaNode = contentNode.putObject("media");

                // For now, we're only handling the first media item (based on the target format)
                LinkedinMedia media = uploadedMedia.get(0);
                mediaNode.put("id", media.getMediaId());
            }

            // Add scheduling if provided
            if (scheduledAt != null) {
                postRequest.put("scheduledStartTime", scheduledAt.getTime());
            }

            System.out.println("Post request: " + postRequest);

            // Prepare and send request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("LinkedIn-Version", "202504");

            HttpEntity<String> request = new HttpEntity<>(postRequest.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("Response: " + response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create LinkedIn post: " + response.getStatusCode() + " - " + response.getBody());
            }

            JsonNode responseNode = mapper.readTree(response.getBody());
            String postId = responseNode.get("id").asText();

            // Create and save LinkedinPost entity
            LinkedinPost linkedinPost = new LinkedinPost();
            linkedinPost.setLinkedinPostId(postId);
            linkedinPost.setCaption(content);
            linkedinPost.setLinkedinMediaList(uploadedMedia);
            linkedinPost.setOrganization(organization);

            // Save the post to the database
            linkedinPostRepository.save(linkedinPost);

            return PlatformPostResult.success(PlatformType.LINKEDIN, linkedinPostRepository.save(linkedinPost));

        } catch (Exception e) {
            e.printStackTrace(); // This gives you the full stack trace
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

        System.out.println("Upload successful");

        System.out.println("Media URN: " + mediaUrn);

        LinkedinMedia media = new LinkedinMedia();
        media.setMediaId(mediaUrn);
        media.setFile(fileData);
        media.setMediaType(PlatformMediaType.IMAGE);

        return linkedinMediaRepository.save(media);
    }
    public LinkedinMedia uploadVideoToLinkedIn(String uploadUrl, FileData fileData, String mediaUrn) throws IOException {

        MediaType fileType;
        switch (fileData.getPrefix()) {
            case "mp4" -> fileType = MediaType.valueOf("video/mp4");
            case "mov" -> fileType = MediaType.valueOf("video/quicktime");
            default -> throw new IllegalArgumentException("Unsupported video type: " + fileData.getPrefix());
        }

        byte[] fileBytes = fileService.getFileBytesByFileData(fileData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(fileType);
        headers.setContentLength(fileBytes.length);
        headers.add("LinkedIn-Version", "202504");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.PUT, requestEntity, String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Video upload failed: " + response.getStatusCode() + " - " + response.getBody());
        }

        LinkedinMedia linkedinMedia = new LinkedinMedia();
        linkedinMedia.setMediaId(mediaUrn);
        linkedinMedia.setFile(fileData);
        linkedinMedia.setMediaType(PlatformMediaType.VIDEO);

        return linkedinMediaRepository.save(linkedinMedia);
    }

}
