package com.MarketingMVP.AllVantage.Entities.PlatformContent.Snapchat;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Snapchat.SnapchatAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapchatReel {

    @Id
    private String snapchatReelId;

    private String caption;

    @OneToOne
    private SnapchatMedia snapchatMedia;

    @ManyToOne
    private SnapchatAccount account;
}
