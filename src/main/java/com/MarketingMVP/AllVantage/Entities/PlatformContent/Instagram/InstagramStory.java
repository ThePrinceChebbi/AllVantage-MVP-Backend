package com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstagramStory {
    @Id
    private String instagramStoryId;

    @OneToOne
    private InstagramMedia instagramMedia;

    @ManyToOne
    private InstagramAccount account;
}
