package com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Account;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "facebook_accounts")
public class FacebookAccount {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "account-generator"
    )
    @SequenceGenerator(
            allocationSize = 1,
            name = "account-generator",
            sequenceName = "account-generator"
    )
    private Long id;

    @NotNull
    private String facebookId;

    @NotNull
    private String accountName;

    @NotNull
    private Date connectedAt;

    @NotNull
    private Date updatedAt;

    @NotNull
    private boolean isGlobal;

    public FacebookAccount(String facebookId, String accountName, Date connectedAt, Date updatedAt, boolean isGlobal) {
        this.facebookId = facebookId;
        this.accountName = accountName;
        this.connectedAt = connectedAt;
        this.updatedAt = updatedAt;
        this.isGlobal = isGlobal;
    }
}
