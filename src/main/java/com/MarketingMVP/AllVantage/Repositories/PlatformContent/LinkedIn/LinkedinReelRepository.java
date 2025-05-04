package com.MarketingMVP.AllVantage.Repositories.PlatformContent.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkedinReelRepository extends JpaRepository<LinkedinReel, Long> {
}
