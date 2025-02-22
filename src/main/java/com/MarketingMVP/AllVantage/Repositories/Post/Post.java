package com.MarketingMVP.AllVantage.Repositories.Post;


import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Post {
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @OneToMany
    private List<FileData> images;

    @NotNull
    private Date createdAt;

    @NotNull
    private Date scheduledToPostAt;

    @NotNull
    private Date lastEditedAt;

    @ManyToOne
    private Employee employee;

    @OneToMany
    private List<FacebookPage> facebookPages;

    @OneToMany
    private List<FacebookAccount> facebookAccounts;

    @OneToMany
    private List<InstagramAccount> instagramAccounts;

    @OneToMany
    private List<LinkedInAccount> linkedInAccounts;

    @OneToMany
    private List<XAccount> xAccounts;

    @OneToMany
    private List<SnapchatAccount> snapchatAccounts;

    @OneToMany
    private List<TikTokAccount> tikTokAccounts;

    public Post(String title, String content, List<FileData> images, Date createdAt, Date scheduledToPostAt, Date lastEditedAt, Employee employee, List<FacebookPage> facebookPages, List<FacebookAccount> facebookAccounts, List<InstagramAccount> instagramAccounts, List<LinkedInAccount> linkedInAccounts, List<XAccount> xAccounts, List<SnapchatAccount> snapchatAccounts, List<TikTokAccount> tikTokAccounts) {
        this.title = title;
        this.content = content;
        this.images = images;
        this.createdAt = createdAt;
        this.scheduledToPostAt = scheduledToPostAt;
        this.lastEditedAt = lastEditedAt;
        this.employee = employee;
        this.facebookPages = facebookPages;
        this.facebookAccounts = facebookAccounts;
        this.instagramAccounts = instagramAccounts;
        this.linkedInAccounts = linkedInAccounts;
        this.xAccounts = xAccounts;
        this.snapchatAccounts = snapchatAccounts;
        this.tikTokAccounts = tikTokAccounts;
    }
}
