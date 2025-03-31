package com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacebookPostRepository extends JpaRepository<FacebookPost, String> {

    @Query(value = "SELECT p FROM FacebookPost p WHERE p.page.id = :pageId")
    List<FacebookPost> findAllByPageId(@Param("pageId") Long pageId);
}
