package com.MarketingMVP.AllVantage.Repositories.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacebookStoryRepository extends JpaRepository<FacebookStory, Long> {
}
