package com.MarketingMVP.AllVantage.Controllers.Accounts;

import com.MarketingMVP.AllVantage.DTOs.Response.PlatformPostResult;
import com.MarketingMVP.AllVantage.Services.Accounts.Facebook.FacebookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/account/facebook")
public class AccountController {
    private final FacebookService facebookService;

    public AccountController(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @GetMapping("/add-global-account")
    public RedirectView facebookAuth() {
        return facebookService.authenticateGlobalAccount();
    }

    @GetMapping("/callback")
    public ResponseEntity<Object> facebookCallback(@RequestParam("code") String code) {
        return facebookService.authenticateGlobalAccountCallback(code);
    }

    @GetMapping("/{accountId}/user-pages")
    public ResponseEntity<Object> getUserPages(@PathVariable Long accountId) {
        return facebookService.getUserPages(accountId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllAccounts() {
        return facebookService.getAllAccounts();
    }

/*    @PostMapping("/{pageId}/post-reel")
    public PlatformPostResult postReel(
            @PathVariable Long pageId,
            @RequestParam String content,
            @RequestParam MultipartFile video,
            @RequestParam Date scheduledAt,
            @RequestParam String title

    ) {
        return facebookService.createFacebookReel(, title, content, scheduledAt, pageId);
    }*/
}
