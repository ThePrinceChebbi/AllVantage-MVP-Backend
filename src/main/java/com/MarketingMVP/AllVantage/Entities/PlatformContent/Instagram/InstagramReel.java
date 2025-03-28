package com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstagramReel {

    @Id
    private String instagramReelId;

    private String caption;

    @OneToOne
    private InstagramMedia instagramMedia;

    @ManyToOne
    private FacebookPage page;
}
