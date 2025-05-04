package com.MarketingMVP.AllVantage.Services.Platform_Specific.LinkedIn;


import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.List;

public interface LinkedInService {

    RedirectView getAuthenticationCode(String redirectUri);

    RedirectView authenticateGlobalAccount();

    @Transactional
    ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode);


    JsonNode fetchAdministeredPages(Long accountId) throws Exception;

    LinkedInOrganization authenticateLinkedInOrganization(Long accountId, String organizationId) throws Exception;

    PlatformPostResult createLinkedInPost(List<FileData> files, String content, Date scheduledAt, Long organizationId);
}
