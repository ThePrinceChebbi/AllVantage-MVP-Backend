package com.MarketingMVP.AllVantage.Entities.PlatformContent.LinkedIn;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.PlatformMediaType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkedinMedia {

    @Id
    private String mediaId;

    @ManyToOne
    private FileData file;

    private PlatformMediaType mediaType; //IMAGE or VIDEO

}
