package com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkedinPost {

    @Id
    private String linkedinPostId;

    private String caption;

    @OneToMany
    private List<LinkedinMedia> linkedinMediaList;

    @ManyToOne
    private LinkedInOrganization organization;

}
