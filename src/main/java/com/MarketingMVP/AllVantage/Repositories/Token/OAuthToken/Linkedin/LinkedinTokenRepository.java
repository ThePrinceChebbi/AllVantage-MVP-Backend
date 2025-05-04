package com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Linkedin;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookAccountToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookTokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.LinkedIn.LinkedinToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LinkedinTokenRepository extends JpaRepository<LinkedinToken,Long> {

    @Query("SELECT f from LinkedinToken f where f.account.id =:accountId")
    List<LinkedinToken> findByAccountId(@Param("accountId") Long accountId);

}
