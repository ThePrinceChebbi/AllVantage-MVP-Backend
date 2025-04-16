package com.MarketingMVP.AllVantage.Repositories.PlatformContent.Instagram;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstagramPostRepository extends JpaRepository<InstagramPost, String> {

    @Query(value = "SELECT p FROM InstagramPost p WHERE p.account.id =:accountId")
    List<InstagramPost> findAllByPageId(@Param("accountId") Long accountId);
}
