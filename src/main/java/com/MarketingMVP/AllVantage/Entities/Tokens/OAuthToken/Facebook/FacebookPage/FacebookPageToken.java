package com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookPage;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facebook_page_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FacebookPageToken {
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "facebook_page_token_sequence"
    )
    @SequenceGenerator(
            name = "facebook_page_token_sequence",
            sequenceName = "facebook_page_token_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    @Column(length = 512)
    private String accessToken;

    @ManyToOne
    private FacebookPage page;

    @NotNull
    private int expiresIn;
}
