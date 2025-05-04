package com.MarketingMVP.AllVantage.DTOs.Facebook.Account;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Account.FacebookAccount;

import java.util.function.Function;

public class FacebookAccountDTOMapper implements Function<FacebookAccount, FacebookAccountDTO> {
    @Override
    public FacebookAccountDTO apply(FacebookAccount facebookAccount) {
        return new FacebookAccountDTO(
                facebookAccount.getId(),
                facebookAccount.getAccountName(),
                facebookAccount.getConnectedAt(),
                facebookAccount.getUpdatedAt()
        );
    }
}
