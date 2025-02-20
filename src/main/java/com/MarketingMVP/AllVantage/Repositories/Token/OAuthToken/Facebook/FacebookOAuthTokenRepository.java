package com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacebookOAuthTokenRepository extends JpaRepository<FacebookOAuthToken,Long> {

    @Query("SELECT f from FacebookOAuthToken f where f.account.id = :accountId and f.oAuthTokenType = :tokenType")
    FacebookOAuthToken findByAccountIdAndTokenType(@Param("accountId") Long accountId, @Param("tokenType") FacebookOAuthTokenType tokenType);

}
