package com.MarketingMVP.AllVantage.Services.Token.FacebookOAuthToken;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookOAuthTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook.FacebookAccountTokenRepository;
import com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook.FacebookPageTokenbRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacebookOAuthTokenServiceImpl implements FacebookOAuthTokenService {

    private final FacebookAccountTokenRepository facebookAccountTokenRepository;
    private final FacebookPageTokenbRepository facebookPageTokenbRepository;

    public FacebookOAuthTokenServiceImpl(FacebookAccountTokenRepository facebookOAuthTokenRepository, FacebookPageTokenbRepository facebookPageTokenbRepository) {
        this.facebookAccountTokenRepository = facebookOAuthTokenRepository;
        this.facebookPageTokenbRepository = facebookPageTokenbRepository;
    }

    @Override
    public FacebookAccountToken saveToken(FacebookAccountToken token) {
        return facebookAccountTokenRepository.save(token);
    }

    @Override
    public FacebookAccountToken getTokensByAccountId(Long accountId) {
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
    public List<FacebookAccountToken> getTokenByAccountIdAndType(Long accountId, FacebookOAuthTokenType tokenType) {
        return facebookAccountTokenRepository.findByAccountIdAndTokenType(accountId, tokenType);
    }

    @Override
    public List<FacebookPageToken> getTokenByPageId(Long pageId) {
        return facebookPageTokenbRepository.findByPageId(pageId);
    }

    @Override
    public FacebookPageToken savePageToken(FacebookPageToken oAuthToken) {
        return facebookPageTokenbRepository.save(oAuthToken);
    }
}
