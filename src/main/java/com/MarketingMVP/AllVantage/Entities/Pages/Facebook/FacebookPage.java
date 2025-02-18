package com.MarketingMVP.AllVantage.Entities.Pages.Facebook;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facebook_pages")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FacebookPage {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "facebook_page_sequence"
    )
    @SequenceGenerator(
            name = "facebook_page_sequence",
            sequenceName = "facebook_page_sequence",
            allocationSize = 1
    )
    private Long id;

    @NotNull
    private String pageId;

    @NotNull
    private String pageName;

    @NotNull
    private String pageCategory;

    private String pageDescription;

    private String pagePicture;

    private String pageCover;

    private String pageWebsite;

    @NotNull
    private String pagePhone;

    @NotNull
    private String pageEmail;

    @NotNull
    private String pageLocation;

    @ManyToOne
    @JoinColumn(name = "facebook_account_id")
    private FacebookAccount facebookAccount;

    @ManyToOne
    @JoinColumn(name = "suit_id")
    private Suit suit;
}
