package com.MarketingMVP.AllVantage.Entities.Suit;

import com.MarketingMVP.AllVantage.Entities.Account.Facebook.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.Account.LinkedIn.LinkedInAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Snapchat.SnapchatAccount;
import com.MarketingMVP.AllVantage.Entities.Account.X.XAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Post.Post;
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

    @ManyToOne
    private Client client;

    @NotNull
    private String name;

    @Nullable
    private String description;

    @ManyToOne
    private FileData image;

    @OneToMany
    private List<Employee> employee;

    @ManyToOne
    private FacebookAccount facebookAccount;

    @ManyToOne
    private InstagramAccount instagramAccount;

    @ManyToOne
    private LinkedInAccount linkedInAccount;

    @ManyToOne
    private XAccount xAccount;

    @ManyToOne
    private SnapchatAccount snapchatAccount;

    @OneToMany
    private List<Post> posts;
}
