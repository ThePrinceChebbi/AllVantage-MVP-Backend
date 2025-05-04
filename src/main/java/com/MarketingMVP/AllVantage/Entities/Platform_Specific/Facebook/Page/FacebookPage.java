package com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Account.FacebookAccount;
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
@Table
public class FacebookPage {
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
    private String pageName;

    @NotNull
    private String facebookPageId;

    @NotNull
    private Date connectedAt;

    @NotNull
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "facebook_account_id")
    private FacebookAccount facebookAccount;

}
