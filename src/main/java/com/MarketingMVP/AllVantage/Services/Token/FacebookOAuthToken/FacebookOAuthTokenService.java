package com.MarketingMVP.AllVantage.Services.Token.FacebookOAuthToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;

import java.util.List;

public interface FacebookOAuthTokenService {
    FacebookAccountToken saveToken(FacebookAccountToken token);
    List<FacebookAccountToken> getTokensByAccountId(Long accountId);
    void deleteToken(Long accountId);
    boolean isTokenValid(String accessToken);

    List<FacebookAccountToken> getTokenByAccountIdAndType(Long accountId, FacebookOAuthTokenType tokenType);

    List<FacebookPageToken> getTokenByPageId(Long pageId);

    FacebookPageToken savePageToken(FacebookPageToken oAuthToken);
}
