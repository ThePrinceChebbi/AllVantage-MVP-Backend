package com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn;

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
public class LinkedinReel {

    @Id
    private String linkedinReelId;

    private String caption;

    @OneToOne
    private LinkedinMedia linkedinMedia;

    @ManyToOne
    private FacebookPage page;
}
