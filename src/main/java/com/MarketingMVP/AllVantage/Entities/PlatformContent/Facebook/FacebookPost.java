package com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
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
public class FacebookPost {

    @Id
    private String facebookPostId;

    private String caption;

    @OneToMany
    private List<FacebookMedia> facebookMediaList;

    @ManyToOne
    private FacebookPage page;

}
