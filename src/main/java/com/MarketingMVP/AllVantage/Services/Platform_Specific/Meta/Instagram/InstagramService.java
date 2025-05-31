package com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import java.util.Date;
import java.util.List;

import com.MarketingMVP.AllVantage.Entities.Platform_Specific.Instagram.InstagramAccount;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

public interface InstagramService {

    ResponseEntity<Object> getPageInstagramAccounts(Long pageId);

    ResponseEntity<Object> getInstagramAccountDetails(String instagramId, Long pageId);

    InstagramAccount addInstagramAccount(String instagramBusinessId, Long pageId) throws JsonProcessingException;

    ResponseEntity<Object> getInstagramProfilePicture(Long id);

    PlatformPostResult createInstagramPost(
            List<FileData> files, // Instagram might only allow single image/video per basic post through the API
            String caption,
            Date scheduledAt,
            InstagramAccount instagramAccount
    );

    PlatformPostResult createInstagramReel(
            FileData video,
            String caption,
            Date scheduledAt,
            InstagramAccount instagramAccount
    );

    PlatformPostResult createInstagramStory(
            FileData media,
            Date scheduledAt,
            Long instagramAccountId
    );

    PlatformInsightsResult getInstagramAccountInsights(Long instagramAccountId, List<String> metrics, String period);

    PlatformInsightsResult getInstagramPostInsights(Long accountId, String mediaId, List<String> metrics);

    PlatformInsightsResult getInstagramReelsInsights(Long accountId, String mediaId, List<String> metricList);

    ResponseEntity<Object> getAllPosts(Long accountId); // Or perhaps get media

    ResponseEntity<Object> getInstagramAccountInfo(Long accountId);

    ResponseEntity<Object> getAllAccounts();

    ResponseEntity<Object> getAllReels(Long accountId);
}
