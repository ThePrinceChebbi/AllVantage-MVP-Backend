package com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Account;


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
@Table(name = "linkedin_accounts")
public class LinkedInAccount {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "linkedin-generator"
    )
    @SequenceGenerator(
            allocationSize = 1,
            name = "linkedin-generator",
            sequenceName = "linkedin-generator"
    )
    private Long id;

    @NotNull
    private String linkedinId;

    @NotNull
    private String accountName;

    @NotNull
    private Date connectedAt;

    @NotNull
    private Date updatedAt;

    public LinkedInAccount(String linkedinId, String accountName, Date connectedAt, Date updatedAt) {
        this.linkedinId = linkedinId;
        this.accountName = accountName;
        this.connectedAt = connectedAt;
        this.updatedAt = updatedAt;
    }
}
