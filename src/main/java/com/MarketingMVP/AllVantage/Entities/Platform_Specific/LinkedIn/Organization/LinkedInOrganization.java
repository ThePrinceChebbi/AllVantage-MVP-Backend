package com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Account.LinkedInAccount;
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
public class LinkedInOrganization {
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
    private String pageName;

    @NotNull
    private String organizationId;

    @NotNull
    private Date connectedAt;

    @NotNull
    private Date updatedAt;

    @ManyToOne
    private LinkedInAccount linkedInAccount;

}
