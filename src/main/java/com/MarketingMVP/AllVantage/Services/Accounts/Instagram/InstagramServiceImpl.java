package com.MarketingMVP.AllVantage.Services.Accounts.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Instagram.Account.InstagramAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.Instagram.Account.InstagramAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Instagram.AccountToken.InstagramTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Instagram.AccountToken.InstagramTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Instagram.InstagramToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.MetaOAuthTokenType;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Instagram.InstagramAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Instagram.InstagramTokenRepository;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class InstagramServiceImpl implements InstagramService{

    private final String redirectUri = "http://localhost:8080/api/v1/account/instagram/callback";
    private final InstagramTokenRepository instagramTokenRepository;
    private final InstagramTokenDTOMapper instagramTokenDTOMapper;
    private final InstagramAccountRepository instagramAccountRepository;
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    /*private final String scope =  "publish_video,pages_show_list," +
            "instagram_manage_insights," +
            "instagram_business_basic," +
            "instagram_business_content_publish," +
            "instagram_business_manage_messages," +
            "instagram_business_manage_comments,"+
            "instagram_basic," +
            "instagram_content_publish";*/

    private final String scope = "pages_show_list,instagram_manage_insights,instagram_basic";


    private final RedisTemplate<String, InstagramTokenDTO> redisAccountTemplate;
    private final AESEncryptionService encryptionService;

    public InstagramServiceImpl(RedisTemplate<String, InstagramTokenDTO> redisAccountTemplate, InstagramTokenRepository instagramTokenRepository, InstagramTokenDTOMapper instagramTokenDTOMapper, InstagramAccountRepository instagramAccountRepository, AESEncryptionService encryptionService) {
        this.redisAccountTemplate = redisAccountTemplate;
        this.instagramTokenRepository = instagramTokenRepository;
        this.instagramTokenDTOMapper = instagramTokenDTOMapper;
        this.instagramAccountRepository = instagramAccountRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public RedirectView authenticateGlobalAccount() {
        String authUrl = "https://www.facebook.com/v22.0/dialog/oauth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scope +
                "&response_type=code";
        return new RedirectView(authUrl);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode) {
        try {
            InstagramAccountDTO account = new InstagramAccountDTOMapper().apply(exchangeCodeForToken(authorizationCode,true, redirectUri));
            return ResponseEntity.ok(account);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getPageInstagramAccount(String pageId) {
        return null;
    }


    @Override
    public PlatformPostResult createInstagramPost(List<FileData> files, String caption, Date scheduledAt, Long instagramAccountId) {
        return null;
    }

    @Override
    public PlatformPostResult createInstagramReel(FileData video, String caption, Date scheduledAt, Long instagramAccountId) {
        return null;
    }

    @Override
    public PlatformPostResult createInstagramStory(FileData media, String caption, Date scheduledAt, Long instagramAccountId) {
        return null;
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
    public ResponseEntity<Object> refreshInstagramToken(Long accountId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getInstagramAccountInfo(Long accountId) {
        return null;
    }

    @Transactional
    @Override
    public InstagramAccount exchangeCodeForToken(String authorizationCode,boolean isGlobal, String redirectUri) throws Exception {
        //get short-lived authentication token
        RestTemplate authRestTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> authresponse = authRestTemplate.postForEntity("https://graph.facebook.com/v22.0/oauth/access_token", request, String.class);

        if (authresponse.getBody() == null) throw new RuntimeException("Instagram response is null.");

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode authJsonNode = objectMapper.readTree(authresponse.getBody());
        String shortLivedToken = authJsonNode.has("access_token") ? authJsonNode.get("access_token").asText() : null;

        if (shortLivedToken == null) throw new RuntimeException("Short-lived access token is missing. Full response: " + authresponse.getBody());

        //find or create new local instagram account
        JsonNode userInfo = fetchUserInfoFromInstagram(List.of("user_id", "name"), shortLivedToken);

        InstagramAccount savedAccount = instagramAccountRepository.findInstagramAccountByInstagramId(userInfo.get("user_id").asText())
                .orElseGet(() -> {
                    InstagramAccount newAccount = new InstagramAccount(
                            userInfo.get("user_id").asText(),
                            userInfo.get("name").asText(),
                            new Date(),
                            new Date(),
                            isGlobal
                    );
                    return instagramAccountRepository.save(newAccount);
                });

        //exchange short-lived token for long-lived token
        String url = String.format(
                "https://graph.facebook.com/v22.0/oauth/access_token" +
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

        InstagramToken savedToken = generateAndSaveToken(longLivedToken, expiresIn, savedAccount);

        cacheToken(savedToken);

        return savedAccount;
    }

    @Override
    public InstagramTokenDTO getAccountCachedToken(Long accountId, MetaOAuthTokenType tokenType) {
        String key = formulateAccountKey(accountId, tokenType);
        List<InstagramTokenDTO> tokenList = redisAccountTemplate.opsForList().range(key, 0, -1);


        if (tokenList == null || tokenList.isEmpty() || tokenList.stream().allMatch((token) -> token.instagramAccountId() == null)) {
            return fetchAccountToken(accountId, tokenType);
        }

        // Identify the token with the longest expiration
        InstagramTokenDTO bestToken = tokenList.stream()
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
    public ResponseEntity<Object> getAllAccounts() {
        return null;
    }

    private JsonNode fetchUserInfoFromInstagram(List<String> requestedFields, String token) throws Exception {
        String fields = String.join(",", requestedFields);
        String url = String.format("https://graph.instagram.com/v22.0/me?fields=%s&access_token=%s",fields, token);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    private InstagramTokenDTO fetchAccountToken(Long accountId, MetaOAuthTokenType tokenType) throws ResourceNotFoundException {
        InstagramToken token;
        token = instagramTokenRepository.findByAccountIdAndTokenType(accountId, tokenType)
                .stream().filter(t -> validateToken(instagramTokenDTOMapper.apply(t)))
                .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId +  ", please authenticate again.");
        }
        return cacheToken(token);
    }

    private InstagramTokenDTO cacheToken(@NotNull InstagramToken token) {
        InstagramTokenDTO tokenDTO = instagramTokenDTOMapper.apply(token);
        String key = formulateAccountKey(token.getAccount().getId(), token.getOAuthTokenType());

        List<InstagramTokenDTO> existingTokens = redisAccountTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisAccountTemplate.delete(key);
        }

        redisAccountTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }

    private InstagramToken generateAndSaveToken(String longLivedToken, int expiresIn, InstagramAccount instagramAccount) {

        InstagramToken oAuthToken = new InstagramToken();
        String encryptedToken;
        try{
            encryptedToken= encryptionService.encrypt(longLivedToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token: " + e.getMessage());
        }
        oAuthToken.setAccessToken(encryptedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setAccount(instagramAccount);
        oAuthToken.setOAuthTokenType(MetaOAuthTokenType.FACEBOOK_LONG_LIVED);

        revokeAllAccountTokens(instagramAccount);

        return instagramTokenRepository.save(oAuthToken);
    }

    private void revokeAllAccountTokens(InstagramAccount account) {
        List<InstagramToken> tokens = instagramTokenRepository.findByAccountId(account.getId());
        for (InstagramToken token : tokens) {
            token.setRevoked(true);
            instagramTokenRepository.save(token);
        }
    }


    private String formulateAccountKey(Long id, MetaOAuthTokenType tokenType) {
        return tokenType.toString() + id.toString();
    }
    private boolean validateToken(InstagramTokenDTO token) {
        if (token == null) return false;
        if (token.expiresIn() == 0) return true;
        long expirationMillis = TimeUnit.MILLISECONDS.convert(token.expiresIn(), token.expiresInTimeUnit());
        return System.currentTimeMillis() <= expirationMillis;
    }

}
