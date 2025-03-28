package com.MarketingMVP.AllVantage.Entities.PlatformContent.X;

import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XReel {

    @Id
    private String xReelId;

    private String caption;

    @OneToOne
    private XMedia xMedia;

    @ManyToOne
    private XAccount account;
}
