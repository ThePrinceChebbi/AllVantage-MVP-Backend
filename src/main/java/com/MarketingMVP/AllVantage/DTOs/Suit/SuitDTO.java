package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTO;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Account.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;

import java.util.List;

public record SuitDTO (
        Long id,
        String name,
        String description,
        FileData image,
        ClientDTO client,
        List<EmployeeDTO> employees,
        List<FacebookPage> facebookPages,
        List<InstagramAccount> instagramAccounts,
        List<LinkedInAccount> linkedInAccounts,
        List<XAccount> xAccounts,
        List<SnapchatAccount> snapchatAccounts,
        List<TikTokAccount> tikTokAccounts

) {
}
