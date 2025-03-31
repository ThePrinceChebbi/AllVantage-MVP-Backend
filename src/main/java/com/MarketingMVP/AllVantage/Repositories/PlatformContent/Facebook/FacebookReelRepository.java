package com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacebookReelRepository extends JpaRepository<FacebookReel, Long> {
}
