package com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.MetaAuth;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookPageRepository;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook.FacebookAccountTokenRepository;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook.FacebookPageTokenRepository;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class MetaAuthServiceImpl implements MetaAuthService {
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    private final String scope =  "publish_video,pages_show_list," +
            "read_insights," +
            "pages_read_engagement," +
            "pages_manage_metadata," +
            "pages_read_user_content," +
            "pages_manage_ads," +
            "pages_manage_posts," +
            "pages_manage_engagement";

    private final String redirectUri = "http://localhost:8080/api/v1/account/facebook/callback";
    private final AESEncryptionService encryptionService;
    private final RedisTemplate<String,FacebookAccountTokenDTO> redisAccountTemplate;
    private final RedisTemplate<String,FacebookPageTokenDTO> redisPageTemplate;
    private final FacebookAccountRepository facebookAccountRepository;
    private final FacebookPageRepository facebookPageRepository;
    private final FacebookAccountTokenRepository facebookAccountTokenRepository;
    private final FacebookPageTokenRepository facebookPageTokenRepository;

    public MetaAuthServiceImpl(AESEncryptionService encryptionService, RedisTemplate<String, FacebookAccountTokenDTO> redisAccountTemplate, RedisTemplate<String, FacebookPageTokenDTO> redisPageTemplate, FacebookAccountTokenRepository facebookOAuthTokenRepository, FacebookPageTokenRepository facebookPageTokenRepository, FacebookAccountRepository facebookAccountRepository, FacebookPageRepository facebookPageRepository) {
        this.encryptionService = encryptionService;
        this.redisAccountTemplate = redisAccountTemplate;
        this.redisPageTemplate = redisPageTemplate;
        this.facebookAccountTokenRepository = facebookOAuthTokenRepository;
        this.facebookPageTokenRepository = facebookPageTokenRepository;
        this.facebookAccountRepository = facebookAccountRepository;
        this.facebookPageRepository = facebookPageRepository;
    }

    @Override
    public RedirectView getAuthenticationCode(String redirectUri) {

        String authUrl = "https://www.facebook.com/v22.0/dialog/oauth" +
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

    @Transactional
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
                            userInfo.get("id").asText(),
                            userInfo.get("name").asText(),
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

        FacebookAccountToken savedToken = generateAndSaveAccountToken(longLivedToken, expiresIn, savedAccount);

        refreshPageTokens(cacheAccountToken(savedToken));

        return savedAccount;
    }

    @Override
    public FacebookPage authenticateFacebookPage(Long accountId, String pageId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookAccountTokenDTO userToken = getAccountCachedToken(accountId, FacebookTokenType.FACEBOOK_LONG_LIVED);

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

        return savePageToken(oAuthToken);
    }

    private FacebookAccountTokenDTO cacheAccountToken(@NotNull FacebookAccountToken token) {
        FacebookAccountTokenDTO tokenDTO = new FacebookAccountTokenDTOMapper(encryptionService).apply(token);
        String key = formulateAccountKey(token.getAccount().getId(), token.getOAuthTokenType());

        List<FacebookAccountTokenDTO> existingTokens = redisAccountTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisAccountTemplate.delete(key);
        }

        redisAccountTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }

    private FacebookPageTokenDTO cachePageToken(FacebookPageToken token) {
        FacebookPageTokenDTO tokenDTO = new FacebookPageTokenDTOMapper(encryptionService).apply(token);

        String key = formulatePageKey(token.getPage().getId());

        List<FacebookPageTokenDTO> existingTokens = redisPageTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisPageTemplate.delete(key);
        }

        redisPageTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }

    private FacebookPage getPageByFacebookId(String facebookId) {
        return facebookPageRepository.findFacebookPageByFacebookId(facebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with Facebook ID: " + facebookId));
    }

    private FacebookAccount getFacebookAccount(Long accountId) {
        return facebookAccountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException(String.format("Account with id :%d not found", accountId)));
    }

    private FacebookAccount getFacebookAccountByFacebookId(String facebookId) {
        return facebookAccountRepository.findFacebookAccountByFacebookId(facebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with Facebook ID: " + facebookId));
    }

    private FacebookAccountToken generateAndSaveAccountToken(String longLivedToken, int expiresIn, FacebookAccount facebookAccount) {

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
        oAuthToken.setOAuthTokenType(FacebookTokenType.FACEBOOK_LONG_LIVED);

        revokeAllAccountTokens(facebookAccount);

        return saveToken(oAuthToken);
    }

    private JsonNode fetchUserInfoFromFacebook(List<String> requestedFields, String token) throws Exception {
        String fields = String.join(",", requestedFields);
        String url = String.format("https://graph.facebook.com/v19.0/me?fields=%s&access_token=%s",fields, token);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    @Override
    public ResponseEntity<Object> refreshAllTokens(FacebookAccountTokenDTO accountToken) {
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
                Map responseBody = response.getBody();

                if (responseBody.containsKey("access_token")) {
                    String newAccessToken = responseBody.get("access_token").toString();

                    int expiresIn = responseBody.containsKey("expires_in")
                            ? Integer.parseInt(responseBody.get("expires_in").toString())
                            : 0; // Assume long-lived if not provided

                    FacebookAccount facebookAccount = getFacebookAccountByFacebookId(accountToken.facebookAccountId());
                    FacebookAccountToken newAccountToken = generateAndSaveAccountToken(newAccessToken, expiresIn, facebookAccount);

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

    public FacebookPageTokenDTO fetchPageToken(Long pageId) {
        List<FacebookPageToken> tokens = getTokenByPageId(pageId);
        FacebookPageToken token = tokens.stream()
                .filter(t -> !t.isRevoked())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid token found for page: " + pageId));

        System.out.println(token);

    /*    if (validateToken(token)==false) {
            throw new ResourceNotFoundException("Token not found for page: " + pageId + ", please authenticate again.");
        }*/

        System.out.println(token.getAccessToken());
        return cachePageToken(token);
    }

    @Override
    public FacebookAccountTokenDTO getAccountCachedToken(Long accountId, FacebookTokenType tokenType) {

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
        if (tokenList.size() > 1) {
            redisPageTemplate.delete(key);
            redisPageTemplate.opsForList().rightPush(key, bestToken);
        }
        return bestToken;
    }

    @Override
    public FacebookAccountTokenDTO fetchAccountToken(Long accountId, FacebookTokenType tokenType) throws ResourceNotFoundException {
        FacebookAccountToken token;
        token = getTokenByAccountIdAndType(accountId, tokenType)
                .stream().filter(t -> validateToken(new FacebookAccountTokenDTOMapper(encryptionService).apply(t)))
                .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId +  ", please authenticate again.");
        }
        return cacheAccountToken(token);
    }

    private List<FacebookPageTokenDTO> refreshPageTokens(FacebookAccountTokenDTO accountToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("https://graph.facebook.com/v22.0/me/accounts?access_token=%s", accountToken.accessToken());

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            System.out.println(response.getBody());

            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Map<String, Object>> pages = (List<Map<String, Object>>) response.getBody().get("data");

                List<FacebookPageTokenDTO> newPageTokens = new ArrayList<>();

                for (Map<String, Object> page : pages) {
                    try{
                        FacebookPage facebookPage = getPageByFacebookId(page.get("id").toString());
                        String pageAccessToken = page.get("access_token").toString();

                        int expiresIn = page.containsKey("expires_in")
                                ? Integer.parseInt(page.get("expires_in").toString())
                                : 0; // Assume long-lived if not provided

                        newPageTokens.add(new FacebookPageTokenDTOMapper(encryptionService).apply(
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

    private boolean validateToken(Object token) {
        if (token == null) return false;

        if (token instanceof FacebookAccountTokenDTO || token instanceof FacebookPageTokenDTO) {
            // Cast and handle based on type

            long expiresIn;
            TimeUnit timeUnit;

            if (token instanceof FacebookAccountTokenDTO accountToken) {
                expiresIn = accountToken.expiresIn();
                timeUnit = accountToken.expiresInTimeUnit();
            } else {
                FacebookPageTokenDTO pageToken = (FacebookPageTokenDTO) token;
                expiresIn = pageToken.expiresIn();
                timeUnit = pageToken.expiresInTimeUnit();
            }

            if (expiresIn == 0) return true;
            long expirationMillis = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(expiresIn, timeUnit);
            return System.currentTimeMillis() <= expirationMillis;
        }

        return false;
    }

    private void revokeAllAccountTokens(FacebookAccount account) {
        List<FacebookAccountToken> tokens = getTokensByAccountId(account.getId());
        for (FacebookAccountToken token : tokens) {
            token.setRevoked(true);
            saveToken(token);
        }
    }

    private void revokeAllPageTokens(FacebookPage page) {
        List<FacebookPageToken> tokens = getTokenByPageId(page.getId());
        for (FacebookPageToken token : tokens) {
            token.setRevoked(true);
            savePageToken(token);
        }
    }

    @Override
    public String formulateAccountKey(Long id, FacebookTokenType tokenType) {
        return tokenType.toString() + id.toString();
    }

    @Override
    public String formulatePageKey(Long id) {
        return "pageId_"+ id.toString();
    }


    @Override
    public FacebookAccountToken saveToken(FacebookAccountToken token) {
        return facebookAccountTokenRepository.save(token);
    }

    @Override
    public List<FacebookAccountToken> getTokensByAccountId(Long accountId) {
        return facebookAccountTokenRepository.findByAccountId(accountId);
    }

    @Override
    public void deleteToken(Long accountId) {

    }

    @Override
    public boolean isTokenValid(String accessToken) {
        return false;
    }

    @Override
    public List<FacebookAccountToken> getTokenByAccountIdAndType(Long accountId, FacebookTokenType tokenType) {
        return facebookAccountTokenRepository.findByAccountIdAndTokenType(accountId, tokenType);
    }

    @Override
    public List<FacebookPageToken> getTokenByPageId(Long pageId) {
        return facebookPageTokenRepository.findByPageId(pageId);
    }

    @Override
    public FacebookPageToken savePageToken(FacebookPageToken oAuthToken) {
        return facebookPageTokenRepository.save(oAuthToken);
    }
}
