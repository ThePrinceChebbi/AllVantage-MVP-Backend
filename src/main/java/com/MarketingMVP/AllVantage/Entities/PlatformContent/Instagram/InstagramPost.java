package com.MarketingMVP.AllVantage.Entities.PlatformContent.Instagram;

import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
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
public class InstagramPost {

    @Id
    private String instagramPostId;

    private String caption;

    @OneToMany
    private List<InstagramMedia> instagramMediaList;

    @ManyToOne
    private InstagramAccount account;

}
