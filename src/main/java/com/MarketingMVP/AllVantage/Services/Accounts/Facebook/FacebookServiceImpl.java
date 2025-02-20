package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.FacebookOAuthTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Account.Facebook.FacebookAccountRepository;
import com.MarketingMVP.AllVantage.Services.OAuthToken.FacebookOAuthTokenService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FacebookServiceImpl implements FacebookService {
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String redirectUri;

    private final RedisTemplate<String,Object> redisTemplate;
    private final FacebookOAuthTokenService facebookOAuthTokenService;
    private final FacebookAccountRepository facebookAccountRepository;

    public FacebookServiceImpl(RedisTemplate<String, Object> redisTemplate, FacebookOAuthTokenService facebookOAuthTokenService, FacebookAccountRepository facebookAccountRepository) {
        this.redisTemplate = redisTemplate;
        this.facebookOAuthTokenService = facebookOAuthTokenService;
        this.facebookAccountRepository = facebookAccountRepository;
    }

    @Override
    public RedirectView getAuthenticationCode() {
        String authUrl = "https://www.facebook.com/v19.0/dialog/oauth" +
                "?client_id=" + clientId +
                "&redirect_uri=http://localhost:8080/api/v1/account/facebook/callback" +
                "&scope=pages_show_list,pages_manage_posts,pages_manage_engagement" +
                "&response_type=code";
        return new RedirectView(authUrl);
    }

    @Transactional
    @Override
    public ResponseEntity<Object> exchangeCodeForToken(String authorizationCode) {
        try{
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

            //create new local facebook account
            JsonNode userInfo = fetchUserInfoFromFacebook(List.of("id", "name"), shortLivedToken);

            FacebookAccount account = new FacebookAccount();
            account.setAccountName(userInfo.get("name").asText());
            account.setFacebookId(userInfo.get("id").asText());
            account.setConnectedAt(new Date());
            account.setUpdatedAt(new Date());

            FacebookAccount savedAccount = facebookAccountRepository.save(account);

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
            FacebookOAuthToken savedToken = generateAndSaveToken(jsonNode, savedAccount);

            cacheToken(savedAccount.getId(), savedToken);

            System.out.println("Facebook Long Lived Token: " + savedToken.getAccessToken());
            System.out.println("Cashed Token: " + getCachedToken(savedAccount.getId(), FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN));

            return ResponseEntity.ok(savedAccount);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error connecting account "+e.getMessage());
        }
    }


    private void cacheToken(Long accountId, @NotNull FacebookOAuthToken token){
        FacebookOAuthTokenDTO tokenDTO = new FacebookOAuthTokenDTO(token.getAccessToken(), token.getExpiresIn(), TimeUnit.SECONDS);
        String key = token.getOAuthTokenType().toString() + accountId;
        redisTemplate.opsForValue().set(key, tokenDTO);
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
    public ResponseEntity<Object> addFacebookPageToSuit(Long suitId,Long accountId) {
        return null;
    }

    @Override
    public FacebookOAuthTokenDTO getCachedToken(Long accountId, FacebookOAuthTokenType tokenType) {
        Object obj = redisTemplate.opsForValue().get(tokenType.toString() + accountId);

        if (obj instanceof FacebookOAuthTokenDTO) {
            return (FacebookOAuthTokenDTO) obj;
        } else {
            return null;
        }
    }

    private FacebookOAuthTokenDTO fetchToken(Long accountId, FacebookOAuthTokenType tokenType) {
        FacebookOAuthToken token = facebookOAuthTokenService.getTokenByAccountIdAndType(accountId,tokenType);
        if (token == null) {
            throw new ResourceNotFoundException("Token not found for account: " + accountId +  ", please authenticate again.");
        }
        cacheToken(accountId, token);
        return new FacebookOAuthTokenDTO(token.getAccessToken(), token.getExpiresIn(), TimeUnit.SECONDS);
    }

    @Override
    public JsonNode fetchUserPages(Long accountId) throws ResourceNotFoundException, JsonProcessingException {
        FacebookOAuthTokenDTO token;
        token = getCachedToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN);
        if (token == null) token = fetchToken(accountId, FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN);
        String url = "https://graph.facebook.com/v19.0/me/accounts?access_token=" + token.accessToken();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch managed pages. Status: " + response.getStatusCode());
        }

        // Parse JSON response
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(response.getBody());
    }

    @Override
    public ResponseEntity<Object> getPageAccessToken(String accessToken) {
        return null;
    }

    private FacebookOAuthToken generateAndSaveToken(JsonNode jsonNode, FacebookAccount savedAccount) {
        String longLivedToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0;

        if (longLivedToken == null) throw new RuntimeException("Long-lived token is missing. Full response: " + jsonNode);
        if (expiresIn == 0) throw new RuntimeException("expiration token is missing. Full response: " + jsonNode);

        FacebookOAuthToken oAuthToken = new FacebookOAuthToken();
        oAuthToken.setAccessToken(longLivedToken);
        oAuthToken.setExpiresIn(expiresIn); //remember to account for that 0 possibility being non expiry
        oAuthToken.setAccount(savedAccount);
        oAuthToken.setOAuthTokenType(FacebookOAuthTokenType.FACEBOOK_LONG_LIVED_USER_ACCESS_TOKEN);
        return facebookOAuthTokenService.saveToken(oAuthToken);
    }

    private JsonNode fetchUserInfoFromFacebook(List<String> requestedFields, String token) throws Exception {
        String fields = String.join(",", requestedFields);
        String url = String.format("https://graph.facebook.com/v19.0/me?fields=%s&access_token=%s",fields, token);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }
}
