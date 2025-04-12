package com.MarketingMVP.AllVantage.Repositories.Account.Instagram;

import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InstagramAccountRepository extends JpaRepository<InstagramAccount,Long> {

    @Query("SELECT i from InstagramAccount i where i.instagramId = :instagramId")
    Optional<InstagramAccount> findInstagramAccountByInstagramId(@Param("instagramId") String instagramId);
}
