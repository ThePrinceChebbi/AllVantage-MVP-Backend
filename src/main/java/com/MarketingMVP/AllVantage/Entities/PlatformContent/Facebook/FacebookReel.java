package com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookReel {

    @Id
    private String facebookVideoId;

    private String caption;

    @ManyToOne
    private FileData file;

    private PlatformMediaType mediaType;

    @ManyToOne
    private FacebookPage page;
}
