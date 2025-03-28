package com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook;

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
public class FacebookReel {

    @Id
    private String facebookReelId;

    private String facebookVideoId;
    private String caption;

    @OneToOne
    private FacebookMedia facebookMedia;

    @ManyToOne
    private FacebookPage page;
}
