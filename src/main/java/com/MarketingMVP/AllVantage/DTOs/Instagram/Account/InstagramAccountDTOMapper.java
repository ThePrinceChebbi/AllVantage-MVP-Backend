package com.MarketingMVP.AllVantage.DTOs.Instagram.Account;

import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;

import java.util.function.Function;

public class InstagramAccountDTOMapper implements Function<InstagramAccount, InstagramAccountDTO> {
    @Override
    public InstagramAccountDTO apply(InstagramAccount instagramAccount) {
        return new InstagramAccountDTO(
                instagramAccount.getId(),
                instagramAccount.getAccountName(),
                instagramAccount.getConnectedAt(),
                instagramAccount.getUpdatedAt()
        );
    }
}
