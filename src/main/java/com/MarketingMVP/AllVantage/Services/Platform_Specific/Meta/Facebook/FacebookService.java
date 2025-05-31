package com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public interface  FacebookService {

    FacebookMedia uploadMediaToFacebook(FileData fileData, Long pageId);

    PlatformPostResult createFacebookPost(
            List<FileData> files,
            @NotNull String title,
            @NotNull String content,
            Long facebookPage
    );

    PlatformPostResult createFacebookReel(
            FileData video,
            @NotNull String title,
            @NotNull String content,
            Long pageId
    );

    PlatformPostResult storyOnFacebookPage(
            FileData story,
            @NotNull String title,
            @NotNull String content,
            Long facebookPageId
    );

    PlatformInsightsResult getFacebookPageInsights(Long pageId, String metricName);

    String getFacebookPageImageUrl(Long pageId);

    PlatformInsightsResult getFacebookPostInsights(Long pageId, String facebookPostId, String metricList);

    ResponseEntity<Object> getAllPosts(Long id);

    ResponseEntity<Object> testRefreshMethod(Long accountId);

    ResponseEntity<Object> getUserPages(Long accountId);

    JsonNode fetchUserPages(Long accountId) throws JsonProcessingException;

    ResponseEntity<Object> getAllAccounts();

    PlatformInsightsResult getFacebookInsights(Long id, Date startDate, Date endDate);

    ResponseEntity<Object> getPagePicture(Long pageId);
}
