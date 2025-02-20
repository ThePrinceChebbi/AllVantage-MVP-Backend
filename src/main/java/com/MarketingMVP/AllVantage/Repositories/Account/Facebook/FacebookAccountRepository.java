package com.MarketingMVP.AllVantage.Repositories.Account.Facebook;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacebookAccountRepository extends JpaRepository<FacebookAccount,Long> {
}
