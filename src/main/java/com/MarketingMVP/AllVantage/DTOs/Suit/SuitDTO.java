package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.DTOs.Instagram.InstagramAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization.LinkedinOrganizationDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTO;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;

import java.util.List;

public record SuitDTO (
        Long id,
        String name,
        String description,
        String imageUrl,
        String suitColor,
        ClientDTO client,
        List<EmployeeDTO> employees,
        List<FacebookPageDTO> facebookPages,
        List<InstagramAccountDTO> instagramAccounts,
        List<LinkedinOrganizationDTO> linkedInOrganizations
) {
}
