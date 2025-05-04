package com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkedinPostRepository extends JpaRepository<LinkedinPost, String> {

    @Query(value = "SELECT p FROM LinkedinPost p WHERE p.organization.id =:orgId")
    List<FacebookPost> findAllByPageId(@Param("orgId") Long orgId);
}
