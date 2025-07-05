package com.MarketingMVP.AllVantage.DTOs.Post.Reel;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;

import java.util.Date;
import java.util.List;

public record ReelDTO(
        Long id,

        String title,

        String content,

        Date createdAt,

        Date scheduledToPostAt,

        Date lastEditedAt,

        UserDTO employee,

        List<FacebookReel> facebookReels,

        List<InstagramReel> instagramReels,

        List<LinkedinReel> linkedinReels
) {
}
