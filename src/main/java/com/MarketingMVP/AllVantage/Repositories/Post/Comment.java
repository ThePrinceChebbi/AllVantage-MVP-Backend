package com.MarketingMVP.AllVantage.Repositories.Post;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "comments_sequence"
    )
    @SequenceGenerator(
            name = "comments_sequence",
            sequenceName = "comments_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private PlatformType platformType;

    @NotNull
    private String authorId;

    @NotNull
    private String content;

    @ManyToOne
    private FileData file;
}
