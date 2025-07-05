package com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.PlatformAccounts.LinkedIn.Account.LinkedInAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "linkedin_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LinkedinToken {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "linkedin_token_sequence"
    )
    @SequenceGenerator(
            name = "linkedin_token_sequence",
            sequenceName = "linkedin_token_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    @Column(length = 512)
    private String accessToken;

    @ManyToOne
    private LinkedInAccount account;

    @NotNull
    private int expiresIn;

    @NotNull
    private boolean isRevoked = false;
}
