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
    private String facebookId;

    @NotNull
    private String accountName;

    @NotNull
    private Date createdAt;

    @NotNull
    private Date updatedAt;
}
