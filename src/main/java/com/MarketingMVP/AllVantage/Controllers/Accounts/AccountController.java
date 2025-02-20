package com.MarketingMVP.AllVantage.Controllers.Accounts;

import com.MarketingMVP.AllVantage.Services.Accounts.Facebook.FacebookService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/account/facebook")
public class AccountController {
    private final FacebookService facebookService;

    public AccountController(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @GetMapping("/code")
    public RedirectView facebookAuth() {
        return facebookService.getAuthenticationCode();
    }

    @GetMapping("/callback")
    public ResponseEntity<Object> facebookCallback(@RequestParam("code") String code) {
        return facebookService.exchangeCodeForToken(code);
    }

    @GetMapping("/{accountId}/user-pages")
    public ResponseEntity<Object> getUserPages(@PathVariable Long accountId) {
        return facebookService.getUserPages(accountId);
    }

}
