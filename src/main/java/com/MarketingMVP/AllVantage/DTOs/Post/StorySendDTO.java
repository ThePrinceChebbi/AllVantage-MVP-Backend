package com.MarketingMVP.AllVantage.DTOs.Post;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Builder
@Getter
public class StorySendDTO {

    @NotNull
    private String title;

    private Date scheduledAt;

    private List<Long> facebookPageIds;

    private List<Long> instagramAccountIds;

}
