package com.MarketingMVP.AllVantage.Repositories.Account.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LinkedInOrganizationRepository extends JpaRepository<LinkedInOrganization,Long> {

    @Query(value = "SELECT l FROM LinkedInOrganization l WHERE l.organizationId =:organizationId")
    Optional<LinkedInOrganization> findByOrganizationId(@Param("organizationId") String organizationId);
}
