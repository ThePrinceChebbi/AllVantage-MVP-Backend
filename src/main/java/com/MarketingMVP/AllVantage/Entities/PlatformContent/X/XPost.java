package com.MarketingMVP.AllVantage.Entities.PlatformContent.X;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.X.XAccount;
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
public class XPost {

    @Id
    private String xPostId;

    private String caption;

    @OneToMany
    private List<XMedia> xMediaList;

    @ManyToOne
    private XAccount account;

}
