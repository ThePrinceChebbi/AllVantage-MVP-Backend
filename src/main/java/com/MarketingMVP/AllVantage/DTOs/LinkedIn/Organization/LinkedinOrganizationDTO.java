package com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization;


import java.util.Date;

public record LinkedinOrganizationDTO(
        Long id,
        String organizationName,
        Date createdTime,
        String organizationUsername,
        Long linkedinAccountId
) {
}
