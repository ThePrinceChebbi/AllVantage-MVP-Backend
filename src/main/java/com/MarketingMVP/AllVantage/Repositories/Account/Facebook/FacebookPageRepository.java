package com.MarketingMVP.AllVantage.Repositories.Account.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Facebook.Page.FacebookPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacebookPageRepository extends JpaRepository<FacebookPage, Long> {

    @Query("SELECT f from FacebookPage f where f.facebookPageId = :facebookPageId")
    Optional<FacebookPage> findFacebookPageByFacebookId(@Param("facebookPageId") String facebookPageId);
}
