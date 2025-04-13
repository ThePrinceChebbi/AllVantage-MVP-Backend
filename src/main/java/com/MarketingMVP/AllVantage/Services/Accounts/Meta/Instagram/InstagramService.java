package com.MarketingMVP.AllVantage.Services.Accounts.Meta.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;

public interface InstagramService {

    ResponseEntity<Object> getPageInstagramAccount(String pageId);

    PlatformPostResult createInstagramPost(
            List<FileData> files, // Instagram might only allow single image/video per basic post through the API
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
            FileData media, // Could be image or video
            String caption, // Might have different limitations for stories
            Date scheduledAt,
            Long instagramAccountId
    );

    PlatformInsightsResult getInstagramAccountInsights(Long instagramAccountId, List<String> metrics, String period); // Period can be day, week, month, lifetime

    PlatformInsightsResult getInstagramMediaInsights(String mediaId, List<String> metrics);

    ResponseEntity<Object> getAllPosts(Long accountId); // Or perhaps get media

    ResponseEntity<Object> getInstagramAccountInfo(Long accountId);

    ResponseEntity<Object> getAllAccounts();
}
