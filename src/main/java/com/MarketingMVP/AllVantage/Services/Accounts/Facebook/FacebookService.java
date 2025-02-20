package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.FacebookOAuthTokenDTO;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.RedirectView;

public interface FacebookService {
    RedirectView getAuthenticationCode();

    @Transactional
    ResponseEntity<Object> exchangeCodeForToken(String authorizationCode);

    ResponseEntity<Object> getUserPages(Long accountId);
    ResponseEntity<Object> addFacebookPageToSuit(Long suitId,Long accountId);

    FacebookOAuthTokenDTO getCachedToken(Long accountId, FacebookOAuthTokenType tokenType);
    JsonNode fetchUserPages(Long accountId) throws JsonProcessingException;
    ResponseEntity<Object> getPageAccessToken(String accessToken);

}
