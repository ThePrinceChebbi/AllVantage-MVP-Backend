package com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Instagram;

import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.MetaOAuthTokenType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instagram_account_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InstagramToken {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "instagram_account_token_sequence"
    )
    @SequenceGenerator(
            name = "instagram_account_token_sequence",
            sequenceName = "instagram_account_token_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    @Column(length = 512)
    private String accessToken;

    @ManyToOne
    private InstagramAccount account;

    @NotNull
    private int expiresIn;

    @NotNull
    private boolean isRevoked = false;

    @NotNull
    private MetaOAuthTokenType oAuthTokenType;
}
