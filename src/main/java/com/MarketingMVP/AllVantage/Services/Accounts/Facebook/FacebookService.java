package com.MarketingMVP.AllVantage.Services.Accounts.Facebook;

import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Account.FacebookAccount;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Tokens.OAuthToken.Facebook.FacebookAccount.FacebookOAuthTokenType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.List;

public interface FacebookService {
    RedirectView authenticateGlobalAccount();
    ResponseEntity<Object> authenticateGlobalAccountCallback(String authorizationCode);

    String uploadMediaToFacebook(FileData fileData, Long accountId);
    String postToFacebookPage(
            Long suitId,
            List<MultipartFile> files,
            @NotNull String title,
            @NotNull String content,
            Date scheduledAt,
            Long facebookPageId
    );

    ResponseEntity<Object> testRefreshMethod(Long accountId);

    RedirectView getAuthenticationCode(String redirectUri);

    @Transactional
    FacebookAccount exchangeCodeForToken(String authorizationCode, boolean isGlobal, String redirectUri) throws Exception;

    ResponseEntity<Object> getUserPages(Long accountId);
    FacebookPage authenticateFacebookPage(Long accountId, String pageId) throws JsonProcessingException;

    FacebookAccountTokenDTO getAccountCachedToken(Long accountId, FacebookOAuthTokenType tokenType);
    FacebookPageTokenDTO getPageCachedToken(Long pageId);

    JsonNode fetchUserPages(Long accountId) throws JsonProcessingException;


}
