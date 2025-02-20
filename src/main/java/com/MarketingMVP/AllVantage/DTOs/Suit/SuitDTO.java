package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
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
