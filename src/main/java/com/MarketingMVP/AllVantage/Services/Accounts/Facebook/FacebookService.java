package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.OAuthToken.FacebookOAuthTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.RedirectView;

public interface FacebookService {
    RedirectView authenticateGlobalAccount();
    ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode);

    Object postToFacebookPage(FacebookPage facebookPage, PostSendDTO postSendDTO) throws JsonProcessingException;

    RedirectView getAuthenticationCode(String redirectUri);
    @Transactional
    FacebookAccount exchangeCodeForToken(String authorizationCode, boolean isGlobal, String redirectUri) throws Exception;

    ResponseEntity<Object> getUserPages(Long accountId);
    FacebookPage authenticateFacebookPage(Long accountId, String pageId) throws JsonProcessingException;

    FacebookOAuthTokenDTO getCachedToken(Long accountId, FacebookOAuthTokenType tokenType);
    JsonNode fetchUserPages(Long accountId) throws JsonProcessingException;


}
