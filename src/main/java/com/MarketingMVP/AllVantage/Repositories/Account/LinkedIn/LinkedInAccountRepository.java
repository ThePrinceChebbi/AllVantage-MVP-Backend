package com.MarketingMVP.AllVantage.Repositories.Account.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Account.LinkedInAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LinkedInAccountRepository extends JpaRepository<LinkedInAccount,Long> {

    @Query("SELECT l FROM LinkedInAccount l WHERE l.linkedinId=:linkedinId")
    Optional<LinkedInAccount> findByLinkedinId(@Param("linkedinId") String linkedinId);

}
