package com.MarketingMVP.AllVantage.Entities.Suit;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.TikTok.TikTokAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

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

    @ManyToMany
    @JoinTable(
            name = "employee_suits",
            joinColumns = @JoinColumn(name = "suit_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> employees;

    @OneToMany
    private List<FacebookAccount> facebookAccounts;

    @OneToMany
    private List<InstagramAccount> instagramAccounts;

    @OneToMany
    private List<LinkedInAccount> linkedInAccounts;

    @OneToMany
    private List<XAccount> xAccounts;

    @OneToMany
    private List<SnapchatAccount> snapchatAccounts;

    @OneToMany
    private List<TikTokAccount> tikTokAccounts;

}
