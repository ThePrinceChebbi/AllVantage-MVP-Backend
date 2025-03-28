package com.MarketingMVP.AllVantage.Entities.PlatformContent.Snapchat;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapchatPost {

    @Id
    private String snapchatPostId;

    private String caption;

    @OneToMany
    private List<SnapchatMedia> snapchatMediaList;

    @ManyToOne
    private SnapchatAccount account;

}
