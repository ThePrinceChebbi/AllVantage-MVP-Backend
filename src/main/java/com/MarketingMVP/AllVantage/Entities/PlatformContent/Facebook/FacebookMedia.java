package com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacebookMedia {

    @Id
    private String mediaId;

    @ManyToOne
    private FileData file;

    private PlatformMediaType mediaType;

}
