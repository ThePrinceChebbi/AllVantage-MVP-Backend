package com.MarketingMVP.AllVantage.Entities.Postable.Reel;


import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
public class Reel extends Postable {

    @ManyToOne
    private FileData video;

    public Reel() {

    }

    public void setVideo(FileData video) {
        if (Objects.equals(video.getType(), "video")) {
            this.video = video;
        } else {
            throw new IllegalArgumentException("The file is not a video");
        }
    }

    public Reel(String title, String content, Date createdAt, Date scheduledToPostAt, Date lastEditedAt, Employee employee, List<FacebookPage> facebookPages, List<InstagramAccount> instagramAccounts, List<LinkedInAccount> linkedInAccounts, List<XAccount> xAccounts, List<SnapchatAccount> snapchatAccounts, List<TikTokAccount> tikTokAccounts, FileData video) {
        super(title, content, createdAt, scheduledToPostAt, lastEditedAt, employee, facebookPages, instagramAccounts, linkedInAccounts, xAccounts, snapchatAccounts, tikTokAccounts);
        this.video = video;
    }
}
