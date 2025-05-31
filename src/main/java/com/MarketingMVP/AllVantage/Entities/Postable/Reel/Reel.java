package com.MarketingMVP.AllVantage.Entities.Postable.Reel;


import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table
public class Reel extends Postable {
    @OneToMany
    private List<FacebookReel> facebookReels;
    @OneToMany
    private List<InstagramReel> instagramReels;
    @OneToMany
    private List<LinkedinReel> linkedinReels;

    public Reel(
            String title,
            String content,
            Date createdAt,
            Date scheduledToPostAt,
            Date lastEditedAt,
            Employee employee,
            List<FacebookReel> facebookReels,
            List<InstagramReel> instagramReels,
            List<LinkedinReel> linkedinReels
    ) {
        super(title, content, createdAt, scheduledToPostAt, lastEditedAt, employee);
        this.facebookReels = facebookReels;
        this.instagramReels = instagramReels;
        this.linkedinReels = linkedinReels;
    }

    public Reel() {

    }
}
