package com.MarketingMVP.AllVantage.Entities.Postable;


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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
public class Postable {
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

    @NotNull
    private Date createdAt;

    @NotNull
    private Date scheduledToPostAt;

    @NotNull
    private Date lastEditedAt;

    @ManyToOne
    private Employee employee;

    @ManyToMany
    private List<FacebookPage> facebookPages;

    @ManyToMany
    private List<InstagramAccount> instagramAccounts;

    @ManyToMany
    private List<LinkedInAccount> linkedInAccounts;

    @ManyToMany
    private List<XAccount> xAccounts;

    @ManyToMany
    private List<SnapchatAccount> snapchatAccounts;

    @ManyToMany
    private List<TikTokAccount> tikTokAccounts;

    public Postable(
            String title,
            String content,
            Date createdAt,
            Date scheduledToPostAt,
            Date lastEditedAt,
            Employee employee,
            List<FacebookPage> facebookPages,
            List<InstagramAccount> instagramAccounts,
            List<LinkedInAccount> linkedInAccounts,
            List<XAccount> xAccounts,
            List<SnapchatAccount> snapchatAccounts,
            List<TikTokAccount> tikTokAccounts
    ) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.scheduledToPostAt = scheduledToPostAt;
        this.lastEditedAt = lastEditedAt;
        this.employee = employee;
        this.facebookPages = facebookPages;
        this.instagramAccounts = instagramAccounts;
        this.linkedInAccounts = linkedInAccounts;
        this.xAccounts = xAccounts;
        this.snapchatAccounts = snapchatAccounts;
        this.tikTokAccounts = tikTokAccounts;
    }
}
