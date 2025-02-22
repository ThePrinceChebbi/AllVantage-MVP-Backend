package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Account.FacebookAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.OAuthToken.FacebookOAuthTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.OAuthToken.FacebookOAuthTokenDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookAccountRepository;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookPageRepository;
import com.MarketingMVP.AllVantage.Security.Utility.AESEncryptionService;
import com.MarketingMVP.AllVantage.Services.Token.FacebookOAuthToken.FacebookOAuthTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class FacebookServiceImpl implements FacebookService {

    private final FacebookOAuthTokenDTOMapper facebookOAuthTokenDTOMapper;
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/api/v1/account/facebook/callback";

    private final RedisTemplate<String,FacebookOAuthTokenDTO> redisTemplate;
    private final FacebookOAuthTokenService facebookOAuthTokenService;
    private final FacebookAccountRepository facebookAccountRepository;
    private final FacebookPageRepository facebookPageRepository;
    private final AESEncryptionService encryptionService;

    public FacebookServiceImpl(RedisTemplate<String, FacebookOAuthTokenDTO> redisTemplate, FacebookOAuthTokenService facebookOAuthTokenService, FacebookAccountRepository facebookAccountRepository, FacebookPageRepository facebookPageRepository, AESEncryptionService encryptionService, FacebookOAuthTokenDTOMapper facebookOAuthTokenDTOMapper) {
        this.redisTemplate = redisTemplate;
        this.facebookOAuthTokenService = facebookOAuthTokenService;
        this.facebookAccountRepository = facebookAccountRepository;
        this.facebookPageRepository = facebookPageRepository;
        this.encryptionService = encryptionService;
        this.facebookOAuthTokenDTOMapper = facebookOAuthTokenDTOMapper;
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
        FacebookOAuthToken savedToken = generateAndSaveToken(jsonNode, savedAccount, null, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN);

        cacheToken(savedAccount.getId(), savedToken);

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


    @Override
    public FacebookPage authenticateFacebookPage(Long accountId, String pageId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookOAuthTokenDTO userToken = getCachedToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN);

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
        FacebookOAuthToken pageToken = generateAndSaveToken(jsonNode, facebookPage.getFacebookAccount(), facebookPage, FacebookOAuthTokenType.FACEBOOK_PAGE_ACCESS_TOKEN);
        cacheToken(facebookPage.getId(), pageToken);

        return facebookPage;
    }


    //Utility public methods -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public FacebookOAuthTokenDTO getCachedToken(Long accountId, FacebookOAuthTokenType tokenType)
            throws ResourceNotFoundException, IllegalStateException {

        String key = tokenType.toString() + accountId;
        List<FacebookOAuthTokenDTO> tokenList = redisTemplate.opsForList().range(key, 0, -1);

        if (tokenList == null || tokenList.isEmpty()) {
            return fetchToken(accountId, tokenType);
        }

        // Identify the token with the longest expiration
        FacebookOAuthTokenDTO bestToken = tokenList.stream()
                .max(Comparator.comparingInt(token -> token.expiresIn() == 0 ? Integer.MAX_VALUE : token.expiresIn()))
                .orElseThrow(() -> new IllegalStateException("Failed to determine the best token for account: " + accountId));

        // If duplicates exist, remove all and keep only the best token
        if (tokenList.size() > 1) {
            redisTemplate.delete(key);
            redisTemplate.opsForList().rightPush(key, bestToken);
        }

        return bestToken;
    }

    @Override
    public JsonNode fetchUserPages(Long accountId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookOAuthTokenDTO token;

        token = getCachedToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN);

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

    private FacebookOAuthTokenDTO cacheToken(Long accountId, @NotNull FacebookOAuthToken token) {
        FacebookOAuthTokenDTO tokenDTO = facebookOAuthTokenDTOMapper.apply(token);
        String key = token.getOAuthTokenType().toString() + accountId;

        List<FacebookOAuthTokenDTO> existingTokens = redisTemplate.opsForList().range(key, 0, -1);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            redisTemplate.delete(key);
        }

        redisTemplate.opsForList().rightPush(key, tokenDTO);
        return tokenDTO;
    }


    private FacebookOAuthToken generateAndSaveToken(JsonNode jsonNode, FacebookAccount savedAccount, FacebookPage savedPage, FacebookOAuthTokenType tokenType) {
        String longLivedToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0;

        if (longLivedToken == null) throw new RuntimeException("Long-lived token is missing. Full response: " + jsonNode);

        FacebookOAuthToken oAuthToken = new FacebookOAuthToken();
        String encryptedToken;
        try{
             encryptedToken= encryptionService.encrypt(longLivedToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token: " + e.getMessage());
        }
        oAuthToken.setAccessToken(encryptedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setAccount(savedAccount);
        oAuthToken.setPage(savedPage);
        oAuthToken.setOAuthTokenType(tokenType);

        return facebookOAuthTokenService.saveToken(oAuthToken);
    }

    private FacebookOAuthTokenDTO fetchToken(Long accountId, FacebookOAuthTokenType tokenType) throws ResourceNotFoundException {
        FacebookOAuthToken token = facebookOAuthTokenService.getTokenByAccountIdAndType(accountId, tokenType)
                .stream().filter(t -> !isTokenExpiredOrNull(facebookOAuthTokenDTOMapper.apply(t)))
                .findFirst().orElse(null);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId +  ", please authenticate again.");
        }
        return cacheToken(accountId, token);
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

    private boolean isTokenExpiredOrNull(FacebookOAuthTokenDTO token) {
        if (token == null) return true;
        if (token.expiresIn() == 0) return false;
        long expirationMillis = TimeUnit.MILLISECONDS.convert(token.expiresIn(), token.expiresInTimeUnit());
        return System.currentTimeMillis() >= expirationMillis;
    }
}
