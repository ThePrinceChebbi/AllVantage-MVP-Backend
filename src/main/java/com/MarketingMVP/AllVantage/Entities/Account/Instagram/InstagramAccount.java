package com.MarketingMVP.AllVantage.Entities.Account.Instagram;

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
@Table(name = "instagram_accounts")
public class InstagramAccount {
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
    private String instagramId;

    @NotNull
    private String accountName;

    @NotNull
    private Date connectedAt;

    @NotNull
    private Date updatedAt;

    @NotNull
    private boolean isGlobal;

    public InstagramAccount(String instagramId, String accountName, Date connectedAt, Date updatedAt, boolean isGlobal) {
        this.instagramId = instagramId;
        this.accountName = accountName;
        this.connectedAt = connectedAt;
        this.updatedAt = updatedAt;
        this.isGlobal = isGlobal;
    }
}
