package com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization;

import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.LinkedIn.Organization.LinkedInOrganization;

import java.util.function.Function;

public class LinkedInOrganizationDTOMapper implements Function<LinkedInOrganization, LinkedinOrganizationDTO> {
    @Override
    public LinkedinOrganizationDTO apply(LinkedInOrganization linkedInOrganization) {
        return new LinkedinOrganizationDTO(
                linkedInOrganization.getId(),
                linkedInOrganization.getPageName(),
                linkedInOrganization.getConnectedAt(),
                linkedInOrganization.getOrganizationUsername(),
                linkedInOrganization.getOrganizationId(),
                linkedInOrganization.getLinkedInAccount().getId()
        );
    }
}
