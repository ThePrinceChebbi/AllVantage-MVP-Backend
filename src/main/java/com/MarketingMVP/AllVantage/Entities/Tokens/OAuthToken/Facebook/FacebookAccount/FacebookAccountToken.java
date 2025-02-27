package com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facebook_account_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FacebookAccountToken {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "facebook_account_token_sequence"
    )
    @SequenceGenerator(
            name = "facebook_account_token_sequence",
            sequenceName = "facebook_account_token_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    @Column(length = 512)
    private String accessToken;

    @ManyToOne
    private FacebookAccount account;

    @NotNull
    private int expiresIn;

    @NotNull
    private boolean isRevoked = false;

    @NotNull
    private FacebookOAuthTokenType oAuthTokenType;

}
