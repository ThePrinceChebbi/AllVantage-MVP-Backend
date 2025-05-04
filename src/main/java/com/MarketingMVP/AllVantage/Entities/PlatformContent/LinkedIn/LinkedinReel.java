package com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkedinReel {

    @Id
    private String linkedinReelId;

    private String caption;

    @OneToOne
    private LinkedinMedia linkedinMedia;

    @ManyToOne
    private LinkedInOrganization organization;
}
