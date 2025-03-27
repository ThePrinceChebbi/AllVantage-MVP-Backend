package com.MarketingMVP.AllVantage.Entities.Postable.Post;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Post extends Postable {
    private List<FileData> files;

    public Post(String title, String content, Date createdAt, Date scheduledToPostAt, Date lastEditedAt, Employee employee, List<FacebookPage> facebookPages, List<InstagramAccount> instagramAccounts, List<LinkedInAccount> linkedInAccounts, List<XAccount> xAccounts, List<SnapchatAccount> snapchatAccounts, List<TikTokAccount> tikTokAccounts, List<FileData> files) {
        super( title, content, createdAt, scheduledToPostAt, lastEditedAt, employee, facebookPages, instagramAccounts, linkedInAccounts, xAccounts, snapchatAccounts, tikTokAccounts);
        this.files = files;
    }

}
