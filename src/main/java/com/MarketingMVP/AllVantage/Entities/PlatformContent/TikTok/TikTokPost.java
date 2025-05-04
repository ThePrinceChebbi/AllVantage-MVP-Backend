package com.MarketingMVP.AllVantage.Entities.PlatformContent.TikTok;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.TikTok.TikTokAccount;
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
public class TikTokPost {

    @Id
    private String caption;

    private String tiktokPostId;

    @OneToMany
    private List<TikTokMedia> tiktokMediaList;

    @ManyToOne
    private TikTokAccount account;

}
