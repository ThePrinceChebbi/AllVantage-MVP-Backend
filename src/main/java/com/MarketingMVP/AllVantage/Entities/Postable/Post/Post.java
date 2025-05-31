package com.MarketingMVP.AllVantage.Entities.Postable.Post;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramPost;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinPost;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
public class Post extends Postable {

    @OneToMany
    private List<FacebookPost> facebookPosts;

    @OneToMany
    private List<InstagramPost> instagramPosts;

    @OneToMany
    private List<LinkedinPost> linkedinPosts;

    public Post(
            String title,
            String content,
            Date createdAt,
            Date scheduledToPostAt,
            Date lastEditedAt,
            Employee employee,
            List<FacebookPost> facebookPosts,
            List<InstagramPost> instagramPosts,
            List<LinkedinPost> linkedinPosts
    ) {
        super(title, content, createdAt, scheduledToPostAt, lastEditedAt, employee);
        this.facebookPosts = facebookPosts;
        this.instagramPosts = instagramPosts;
        this.linkedinPosts = linkedinPosts;
    }


}
