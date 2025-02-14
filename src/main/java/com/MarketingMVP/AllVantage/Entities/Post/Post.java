package com.MarketingMVP.AllVantage.Entities.Post;

import com.MarketingMVP.AllVantage.Entities.Account.PlatformType;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Post {
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @OneToMany
    private List<FileData> images;

    private Enum<PlatformType> postedPlatforms;

    private Date createdAt;

    private Date scheduledToPostAt;

    private Date lastEditedAt;

    private int interactions;

    private int views;

    private int shares;

    @OneToMany
    private List<Comment> comments;

}
