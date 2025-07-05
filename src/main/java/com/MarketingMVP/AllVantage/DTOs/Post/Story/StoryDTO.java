package com.MarketingMVP.AllVantage.DTOs.Post.Story;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;

import java.util.Date;
import java.util.List;

public record StoryDTO(
        Long id,

        String title,

        Date createdAt,

        Date scheduledToPostAt,

        Date lastEditedAt,

        UserDTO employee,

        List<FacebookStory> facebookStories,

        List<InstagramStory> instagramStories

) {
}
