package com.MarketingMVP.AllVantage.Entities.Account;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class Account {
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
    private String id;

    @NotNull
    private PlatformType platform;

    @NotNull
    private String accountId;

    @NotNull
    private String accountName;

    @NotNull
    private Date createdAt;

    @NotNull
    private Date updatedAt;
}
