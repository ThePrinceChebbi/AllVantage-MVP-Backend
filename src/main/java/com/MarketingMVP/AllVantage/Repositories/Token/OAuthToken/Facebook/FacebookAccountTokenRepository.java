package com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookTokenType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FacebookAccountTokenRepository extends JpaRepository<FacebookAccountToken,Long> {

    @Query("SELECT f from FacebookAccountToken f where f.account.id = :accountId and f.oAuthTokenType = :tokenType")
    List<FacebookAccountToken> findByAccountIdAndTokenType(@Param("accountId") Long accountId, @Param("tokenType") FacebookTokenType tokenType);

    @Query("SELECT f from FacebookAccountToken f where f.account.id =:accountId")
    List<FacebookAccountToken> findByAccountId(@Param("accountId") Long accountId);
}
