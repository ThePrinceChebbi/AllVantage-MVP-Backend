package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTO;
import com.MarketingMVP.AllVantage.DTOs.Instagram.InstagramAccountDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization.LinkedinOrganizationDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;

import java.util.List;

public record SuitDTO (
        Long id,
        String name,
        String description,
        String imageUrl,
        String suitColor,
        boolean isActive,
        UserDTO client,
        List<UserDTO> employees,
        List<FacebookPageDTO> facebookPages,
        List<InstagramAccountDTO> instagramAccounts,
        List<LinkedinOrganizationDTO> linkedInOrganizations
) {
}
