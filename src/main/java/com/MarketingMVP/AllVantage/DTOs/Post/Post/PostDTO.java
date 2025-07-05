package com.MarketingMVP.AllVantage.DTOs.Post.Post;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;

import java.util.Date;
import java.util.List;

public record PostDTO(
        Long id,

        String title,

        String content,

        Date createdAt,

        Date scheduledToPostAt,

        Date lastEditedAt,

        UserDTO employee,

        List<FacebookPost>facebookPosts,

        List<InstagramPost> instagramPosts,

        List<LinkedinPost> linkedinPosts
) {
}
