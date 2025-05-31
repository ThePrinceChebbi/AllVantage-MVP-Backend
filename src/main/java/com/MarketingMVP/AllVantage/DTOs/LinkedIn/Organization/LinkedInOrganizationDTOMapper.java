package com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;

import java.util.function.Function;

public class LinkedInOrganizationDTOMapper implements Function<LinkedInOrganization, LinkedinOrganizationDTO> {
    @Override
    public LinkedinOrganizationDTO apply(LinkedInOrganization linkedInOrganization) {
        return new LinkedinOrganizationDTO(
                linkedInOrganization.getId(),
                linkedInOrganization.getPageName(),
                linkedInOrganization.getConnectedAt(),
                linkedInOrganization.getOrganizationUsername(),
                linkedInOrganization.getLinkedInAccount().getId()
        );
    }
}
