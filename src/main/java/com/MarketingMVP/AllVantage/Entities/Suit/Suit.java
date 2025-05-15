package com.MarketingMVP.AllVantage.Entities.Suit;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.LinkedIn.Organization.LinkedInOrganization;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Platform_Specific.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Suit {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "suit_sequence"
    )
    @SequenceGenerator(
            name = "suit_sequence",
            sequenceName = "suit_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private String name;

    @Nullable
    private String description;

    @OneToOne
    private FileData image;

    @ManyToOne
    private Client client;

    private String suitColor;

    @ManyToMany
    @JoinTable(
            name = "employee_suits",
            joinColumns = @JoinColumn(name = "suit_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> employees = new ArrayList<>();

    @OneToMany
    private List<FacebookPage> facebookPages;

    @OneToMany
    private List<InstagramAccount> instagramAccounts;

    @OneToMany
    private List<LinkedInOrganization> linkedInOrganizations;

    @OneToMany
    private List<Postable> posts;

}
