package com.MarketingMVP.AllVantage.Repositories.Account.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Facebook.Account.FacebookAccount;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FacebookAccountRepository extends JpaRepository<FacebookAccount,Long> {

    @Query("SELECT f from FacebookAccount f where f.facebookId = :facebookId")
    Optional<FacebookAccount> findFacebookAccountByFacebookId(@Param("facebookId") String facebookId);
}
