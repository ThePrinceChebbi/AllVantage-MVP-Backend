package com.MarketingMVP.AllVantage.DTOs.LinkedIn.Account;


import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Account.LinkedInAccount;

import java.util.function.Function;

public class LinkedInAccountDTOMapper implements Function<LinkedInAccount, LinkedInAccountDTO> {
    @Override
    public LinkedInAccountDTO apply(LinkedInAccount linkedInAccount) {
        return new LinkedInAccountDTO(
                linkedInAccount.getId(),
                linkedInAccount.getAccountName(),
                linkedInAccount.getConnectedAt(),
                linkedInAccount.getUpdatedAt()
        );
    }
}
