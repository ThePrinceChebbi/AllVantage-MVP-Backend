package com.MarketingMVP.AllVantage.Services.Accounts.Meta.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class InstagramServiceImpl implements InstagramService{

    @Override
    public ResponseEntity<Object> getPageInstagramAccount(String pageId) {
        return null;
    }

    @Override
    public PlatformPostResult createInstagramPost(List<FileData> files, String caption, Date scheduledAt, Long instagramAccountId) {
        return null;
    }

    @Override
    public PlatformPostResult createInstagramReel(FileData video, String caption, Date scheduledAt, Long instagramAccountId) {
        return null;
    }

    @Override
    public PlatformPostResult createInstagramStory(FileData media, String caption, Date scheduledAt, Long instagramAccountId) {
        return null;
    }

    @Override
    public PlatformInsightsResult getInstagramAccountInsights(Long instagramAccountId, List<String> metrics, String period) {
        return null;
    }

    @Override
    public PlatformInsightsResult getInstagramMediaInsights(String mediaId, List<String> metrics) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getAllPosts(Long accountId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getInstagramAccountInfo(Long accountId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> getAllAccounts() {
        return null;
    }

}
