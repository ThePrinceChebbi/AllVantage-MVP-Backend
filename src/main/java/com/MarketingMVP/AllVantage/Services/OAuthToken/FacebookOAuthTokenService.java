package com.MarketingMVP.AllVantage.Services.OAuthToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;

public interface FacebookOAuthTokenService {
    FacebookOAuthToken saveToken(FacebookOAuthToken token);
    FacebookOAuthToken getTokensByAccountId(Long accountId);
    void deleteToken(Long accountId);
    boolean isTokenValid(String accessToken);

    FacebookOAuthToken getTokenByAccountIdAndType(Long accountId, FacebookOAuthTokenType tokenType);
}
