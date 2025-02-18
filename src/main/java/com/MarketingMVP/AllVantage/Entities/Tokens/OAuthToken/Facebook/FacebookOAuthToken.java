package com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.FacebookAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facebook_oauth_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FacebookOAuthToken {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "facebook_oauth_token_sequence"
    )
    @SequenceGenerator(
            name = "facebook_oauth_token_sequence",
            sequenceName = "facebook_oauth_token_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private String accessToken;

    @ManyToOne
    private FacebookAccount account;

    @NotNull
    private int expiresIn;

    @NotNull
    private FacebookOAuthTokenType oAuthTokenType;
}
