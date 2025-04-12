package com.MarketingMVP.AllVantage.Services.Accounts.Instagram;

import com.MarketingMVP.AllVantage.DTOs.Instagram.AccountToken.InstagramTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Account.Instagram.InstagramAccount;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import java.util.Date;
import java.util.List;

import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.MetaOAuthTokenType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

public interface InstagramService {

    RedirectView authenticateGlobalAccount();

    ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode);

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

    // Method to refresh access token if needed
    ResponseEntity<Object> refreshInstagramToken(Long accountId);

    // Method to fetch user's Instagram Business account information
    ResponseEntity<Object> getInstagramAccountInfo(Long accountId);

    // Method to handle the token exchange
    InstagramAccount exchangeCodeForToken(String authorizationCode, boolean isGlobal, String redirectUri) throws Exception;

    InstagramTokenDTO getAccountCachedToken(Long accountId, MetaOAuthTokenType tokenType); // Assuming you'll store account details in an entity

    ResponseEntity<Object> getAllAccounts();
}
