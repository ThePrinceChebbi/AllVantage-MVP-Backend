package com.MarketingMVP.AllVantage.Services.OAuthToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;

import java.util.List;

public interface FacebookOAuthTokenService {
    FacebookOAuthToken saveToken(FacebookOAuthToken token);
    FacebookOAuthToken getTokensByAccountId(Long accountId);
    void deleteToken(Long accountId);
    boolean isTokenValid(String accessToken);

    List<FacebookOAuthToken> getTokenByAccountIdAndType(Long accountId, FacebookOAuthTokenType tokenType);
}
