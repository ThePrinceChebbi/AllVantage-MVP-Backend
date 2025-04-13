package com.MarketingMVP.AllVantage.Services.Accounts.Meta.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Response.Insights.PlatformInsightsResult;
import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.FacebookTokenType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.List;

public interface  FacebookService {

    FacebookMedia uploadMediaToFacebook(FileData fileData, Long pageId);

    PlatformPostResult createFacebookPost(
            List<FileData> files,
            @NotNull String title,
            @NotNull String content,
            Date scheduledAt,
            Long facebookPage
    );

    PlatformPostResult createFacebookReel(
            FileData video,
            @NotNull String title,
            @NotNull String content,
            Date scheduledAt,
            Long pageId
    );

    PlatformPostResult storyOnFacebookPage(
            FileData story,
            @NotNull String title,
            @NotNull String content,
            Date scheduledAt,
            Long facebookPageId
    );

    PlatformInsightsResult getFacebookPageInsights(Long pageId, String metricName);

    PlatformInsightsResult getFacebookPostInsights(Long pageId, String facebookPostId, String metricList);

    ResponseEntity<Object> getAllPosts(Long id);

    ResponseEntity<Object> testRefreshMethod(Long accountId);

    ResponseEntity<Object> getUserPages(Long accountId);

    JsonNode fetchUserPages(Long accountId) throws JsonProcessingException;

    ResponseEntity<Object> getAllAccounts();
}
