package com.MarketingMVP.AllVantage.DTOs.Facebook.Page;

import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Facebook.Page.FacebookPage;

import java.util.function.Function;

public class FacebookPageDTOMapper implements Function<FacebookPage, FacebookPageDTO> {

    @Override
    public FacebookPageDTO apply(FacebookPage facebookPage) {
        return new FacebookPageDTO(
                facebookPage.getId(),
                facebookPage.getPageName(),
                facebookPage.getConnectedAt(),
                facebookPage.getPageUrl()
        );
    }
}
