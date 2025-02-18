package com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken;

import com.MarketingMVP.AllVantage.DTOs.Token.Facebook.FacebookOAuthTokenDTO;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookOAuthTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacebookOAuthTokenRepository extends JpaRepository<FacebookOAuthToken,Long> {

    @Query("SELECT f from FacebookOAuthToken f where f.account.id = :accountId and f.oAuthTokenType = :tokenType")
    FacebookOAuthToken findByAccountIdAndTokenType(@Param("accountId") Long accountId,@Param("tokenType") FacebookOAuthTokenType tokenType);

}
