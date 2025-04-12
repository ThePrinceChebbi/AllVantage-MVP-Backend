package com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Instagram;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.MetaOAuthTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Instagram.InstagramToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstagramTokenRepository extends JpaRepository<InstagramToken,Long> {

    @Query("SELECT i from InstagramToken i where i.account.id = :accountId and i.oAuthTokenType = :tokenType")
    List<InstagramToken> findByAccountIdAndTokenType(@Param("accountId") Long accountId, @Param("tokenType") MetaOAuthTokenType tokenType);

    @Query("SELECT i from InstagramToken i where i.account.id =:accountId")
    List<InstagramToken> findByAccountId(@Param("accountId") Long accountId);
}
