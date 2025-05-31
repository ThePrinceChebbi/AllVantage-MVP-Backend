package com.MarketingMVP.AllVantage.Entities.Postable.Story;


import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookStory;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramStory;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Entity
@Table
public class Story extends Postable {
    @OneToMany
    private List<FacebookStory> facebookStories;
    @OneToMany
    private List<InstagramStory> instagramStories;


    public Story(
            String title,
            String content,
            Date createdAt,
            Date scheduledToPostAt,
            Date lastEditedAt,
            Employee employee,
            List<FacebookStory> facebookStories,
            List<InstagramStory> instagramStories
    ) {
        super(title, content, createdAt, scheduledToPostAt, lastEditedAt, employee);
        this.facebookStories = facebookStories;
        this.instagramStories = instagramStories;
    }

    public Story() {

    }
}
