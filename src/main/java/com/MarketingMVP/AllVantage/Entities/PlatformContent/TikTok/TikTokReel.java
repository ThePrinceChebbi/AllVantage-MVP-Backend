package com.MarketingMVP.AllVantage.Entities.PlatformContent.TikTok;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokReel {

    @Id
    private String facebookReelId;

    private String caption;

    @OneToOne
    private TikTokMedia linkedinMedia;

    @ManyToOne
    private TikTokAccount account;
}
