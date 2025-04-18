package com.MarketingMVP.AllVantage.Repositories.PlatformContent.Instagram;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstagramReelRepository extends JpaRepository<InstagramReel, String> {

    @Query(value = "SELECT r FROM InstagramReel r WHERE r.account.id =:accountId")
    List<InstagramReel> findAllByAccountId(@Param("accountId") Long accountId);
}
