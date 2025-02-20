package com.MarketingMVP.AllVantage.Services.OAuthToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook.FacebookOAuthTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class FacebookOAuthTokenServiceImpl implements FacebookOAuthTokenService {

    private final FacebookOAuthTokenRepository facebookOAuthTokenRepository;

    public FacebookOAuthTokenServiceImpl(FacebookOAuthTokenRepository facebookOAuthTokenRepository) {
        this.facebookOAuthTokenRepository = facebookOAuthTokenRepository;
    }

    @Override
    public FacebookOAuthToken saveToken(FacebookOAuthToken token) {
        return facebookOAuthTokenRepository.save(token);
    }

    @Override
    public FacebookOAuthToken getTokensByAccountId(Long accountId) {
        return null;
    }

    @Override
    public void deleteToken(Long accountId) {

    }

    @Override
    public boolean isTokenValid(String accessToken) {
        return false;
    }

    @Override
    public FacebookOAuthToken getTokenByAccountIdAndType(Long accountId, FacebookOAuthTokenType tokenType) {
        return facebookOAuthTokenRepository.findByAccountIdAndTokenType(accountId, tokenType);
    }
}
