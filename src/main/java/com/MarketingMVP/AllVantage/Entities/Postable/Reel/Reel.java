package com.MarketingMVP.AllVantage.Entities.Postable.Reel;


import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram.InstagramReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn.LinkedinReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Snapchat.SnapchatReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.TikTok.TikTokReel;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.X.XReel;
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
public class Reel extends Postable {
    @OneToMany
    private List<FacebookReel> facebookReels;
    @OneToMany
    private List<InstagramReel> instagramReels;
    @OneToMany
    private List<SnapchatReel> snapchatReels;
    @OneToMany
    private List<XReel> xReels;
    @OneToMany
    private List<LinkedinReel> linkedinReels;
    @OneToMany
    private List<TikTokReel> tikTokReels;

    public Reel(
            String title,
            String content,
            Date createdAt,
            Date scheduledToPostAt,
            Date lastEditedAt,
            Employee employee,
            List<FacebookReel> facebookReels,
            List<InstagramReel> instagramReels,
            List<SnapchatReel> snapchatReels,
            List<XReel> xReels,
            List<LinkedinReel> linkedinReels,
            List<TikTokReel> tikTokReels
    ) {
        super(title, content, createdAt, scheduledToPostAt, lastEditedAt, employee);
        this.facebookReels = facebookReels;
        this.instagramReels = instagramReels;
        this.snapchatReels = snapchatReels;
        this.xReels = xReels;
        this.linkedinReels = linkedinReels;
        this.tikTokReels = tikTokReels;
    }

    public Reel() {

    }
}
