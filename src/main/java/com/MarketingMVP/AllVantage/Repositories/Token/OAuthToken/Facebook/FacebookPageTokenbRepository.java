package com.MarketingMVP.AllVantage.Repositories.Token.OAuthToken.Facebook;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage.FacebookPageToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FacebookPageTokenbRepository extends JpaRepository<FacebookPageToken, Long> {
    @Query("SELECT f from FacebookPageToken f where f.page.id =:pageId")
    List<FacebookPageToken> findByPageId(@Param("pageId") Long pageId);
}
