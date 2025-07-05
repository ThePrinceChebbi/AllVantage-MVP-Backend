package com.MarketingMVP.AllVantage.Services.Platform_Specific.LinkedIn;


import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.LinkedIn.Organization.LinkedInOrganization;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

public interface LinkedInService {

    RedirectView getAuthenticationCode(String redirectUri);

    RedirectView authenticateGlobalAccount();

    @Transactional
    RedirectView authenticateGlobalAccountCallback(String authorizationCode);

    JsonNode fetchAdministeredPages(Long accountId) throws Exception;

    LinkedInOrganization authenticateLinkedInOrganization(Long accountId, String organizationId) throws Exception;

    PlatformPostResult createLinkedInPost(List<FileData> files, String content, LinkedInOrganization organization, boolean isReel);

    PlatformInsightsResult getLinkedInInsights(Long organizationId, String shareUrn);

    ResponseEntity<Object> getLinkedInOrgLogo400x400(Long accountId, String orgId);

    ResponseEntity<Object> getAccountProfilePicture(Long accountId);

    String deleteLinkedInPost(LinkedinPost linkedinPost);

    String deleteLinkedInReel(LinkedinReel linkedinReel);

    ResponseEntity<Object> getAllLinkedInAccounts();
}
