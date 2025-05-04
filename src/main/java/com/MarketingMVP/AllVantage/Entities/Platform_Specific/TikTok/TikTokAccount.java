package com.MarketingMVP.AllVantage.Entities.Platform_Specific.TikTok;

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
@Table(name = "tiktok_accounts")
public class TikTokAccount {
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
    private String tiktokId;

    @NotNull
    private String accountName;

    @NotNull
    private Date createdAt;

    @NotNull
    private Date updatedAt;
}
