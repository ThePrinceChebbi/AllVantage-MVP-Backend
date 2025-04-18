package com.MarketingMVP.AllVantage.Services.Accounts.Meta.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;

public interface InstagramService {

    ResponseEntity<Object> getPageInstagramAccounts(Long pageId);

    ResponseEntity<Object> getInstagramAccountDetails(String instagramId, Long pageId);

    ResponseEntity<Object> addInstagramAccount(String instagramBusinessId, Long pageId);

    PlatformPostResult createInstagramPost(
            List<FileData> files, // Instagram might only allow single image/video per basic post through the API
            String title,
            String caption,
            Date scheduledAt,
            Long instagramAccountId
    );

    PlatformPostResult createInstagramReel(
            FileData video,
            String caption,
            Date scheduledAt,
            Long instagramAccountId
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
