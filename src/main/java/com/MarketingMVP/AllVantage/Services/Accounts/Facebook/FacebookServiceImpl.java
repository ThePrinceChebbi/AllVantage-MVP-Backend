package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookPageRepository;
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
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class FacebookServiceImpl implements FacebookService {

    private final FileService fileService;
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

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
                "&scope=pages_show_list,pages_manage_posts,pages_manage_engagement" +
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
    public String uploadMediaToFacebook(FileData fileData, Long pageId) {
        FacebookPageTokenDTO tokenDTO = fetchPageToken(pageId);
        RestTemplate restTemplate = new RestTemplate();

        String metaFileType = fileData.getType().contains("image") ? "photos" : "videos";
        String url = String.format("https://graph.facebook.com/v22.0/%s/%s", tokenDTO.facebookPageId(), metaFileType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 🔥 Read the file & send it directly
        File file = new File(fileData.getPath());
        FileSystemResource fileResource = new FileSystemResource(file);
        body.add("source", fileResource);

        body.add("published", false);
        body.add("access_token", tokenDTO.accessToken());

        try {
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            return Objects.requireNonNull(response.getBody()).get("id").toString();
        } catch (Exception e) {
            return "Failed to upload media: " + e.getMessage();
        }
    }


    @Override
    public Object postToFacebookPage(FacebookPage facebookPage, PostSendDTO postSendDTO) throws JsonProcessingException {
        return null;
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
        FacebookAccountToken savedToken = generateAndSaveAccountToken(jsonNode, savedAccount, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED);

        cacheAccountToken(savedToken);

        return savedAccount;
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
                            page.get("name").asText(),
                            page.get("access_token").asText()
                    );
                    pageList.add(dto);
                }
            }

            return ResponseEntity.ok(pageList);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching user pages: " + e.getMessage());
        }
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
        FacebookPageToken pageToken = generateAndSavePageToken(jsonNode, facebookPage);
        cachePageToken(pageToken);

        return facebookPage;
    }

    private FacebookPageToken generateAndSavePageToken(JsonNode jsonNode, FacebookPage facebookPage) {
        String pageToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0;

        if (pageToken == null) throw new RuntimeException("Long-lived token is missing. Full response: " + jsonNode);

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

        return facebookOAuthTokenService.savePageToken(oAuthToken);
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

    //Utility public methods -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public FacebookPageTokenDTO getPageCachedToken(Long pageId)
            throws ResourceNotFoundException, IllegalStateException {

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

    private FacebookPageTokenDTO fetchPageToken(Long pageId) {
        FacebookPageToken token;
        List<FacebookPageToken> tokens = facebookOAuthTokenService.getTokenByPageId(pageId);
        token = tokens.stream()
                .filter(t -> validatePageToken(facebookPageTokenDTOMapper.apply(t)))
                .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for page: " + pageId + ", please authenticate again.");
        }
        return cachePageToken(token);
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

    //Utility private methods -------------------------------------------------------------------------------------------------------------------------------

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

    private FacebookAccountToken generateAndSaveAccountToken(JsonNode jsonNode, FacebookAccount savedAccount, FacebookOAuthTokenType tokenType) {
        String longLivedToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0;

        if (longLivedToken == null) throw new RuntimeException("Long-lived token is missing. Full response: " + jsonNode);

        FacebookAccountToken oAuthToken = new FacebookAccountToken();
        String encryptedToken;
        try{
             encryptedToken= encryptionService.encrypt(longLivedToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token: " + e.getMessage());
        }
        oAuthToken.setAccessToken(encryptedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setAccount(savedAccount);
        oAuthToken.setOAuthTokenType(tokenType);

        return facebookOAuthTokenService.saveToken(oAuthToken);
    }

    private FacebookAccountTokenDTO fetchAccountToken(Long accountId, FacebookOAuthTokenType tokenType) throws ResourceNotFoundException {
        FacebookAccountToken token;
            token = facebookOAuthTokenService.getTokenByAccountIdAndType(accountId, tokenType)
                    .stream().filter(t -> !validateAccountToken(facebookAccountTokenDTOMapper.apply(t)))
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
