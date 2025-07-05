package com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.Facebook.Page.FacebookPage;
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
public class FacebookStory {
    @Id
    private String facebookStoryId;

    @OneToOne
    private FacebookMedia facebookMedia;

    @ManyToOne
    private FacebookPage page;
}
