package com.MarketingMVP.AllVantage.DTOs.Post;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;
@Builder
@Getter
public class PostSendDTO {

    @NotNull
    private String title;

    @NotNull
    private String content;

    private Date scheduledAt;

    private List<Long> facebookPageIds;

    private List<Long> instagramAccountIds;

    private List<Long> linkedInAccountIds;

    private List<Long> xAccountIds;

    private List<Long> snapchatAccountIds;

    private List<Long> tikTokAccountIds;
}
