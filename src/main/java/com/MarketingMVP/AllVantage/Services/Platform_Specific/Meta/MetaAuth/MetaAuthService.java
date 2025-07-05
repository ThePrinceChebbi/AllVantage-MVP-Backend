package com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.MetaAuth;

import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

public interface MetaAuthService {

    RedirectView getAuthenticationCode(String redirectUri);

    RedirectView authenticateGlobalAccount();

    RedirectView authenticateGlobalAccountCallback(String authorizationCode);

    FacebookPage authenticateFacebookPage(Long accountId, String pageId) throws JsonProcessingException;

    String formulateAccountKey(Long accountId,FacebookTokenType tokenType);

    String formulatePageKey(Long pageId);

    FacebookAccountTokenDTO fetchAccountToken(Long accountId, FacebookTokenType tokenType);

    FacebookPageTokenDTO fetchPageToken(Long accountId);

    ResponseEntity<Object> refreshAllTokens(FacebookAccountTokenDTO accountToken);

    FacebookAccountTokenDTO getAccountCachedToken(Long accountId, FacebookTokenType tokenType);

    FacebookPageTokenDTO getPageCachedToken(Long pageId);

    FacebookAccountToken saveToken(FacebookAccountToken token);
    List<FacebookAccountToken> getTokensByAccountId(Long accountId);
    void deleteToken(Long accountId);
    boolean isTokenValid(String accessToken);

    List<FacebookAccountToken> getTokenByAccountIdAndType(Long accountId, FacebookTokenType tokenType);

    List<FacebookPageToken> getTokenByPageId(Long pageId);

    FacebookPageToken savePageToken(FacebookPageToken oAuthToken);
}
