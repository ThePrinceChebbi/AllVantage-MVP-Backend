package com.MarketingMVP.AllVantage.Controllers.Account.FacebookAccount;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account/facebook")
public class FacebookAccountController {

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    @GetMapping("/code")
    public RedirectView facebookAuth() {
        String authUrl = "https://www.facebook.com/v19.0/dialog/oauth" +
                "?client_id=" + clientId +
                "&redirect_uri=http://localhost:8080/api/v1/account/facebook/callback" +
                "&scope=pages_show_list,pages_manage_posts,pages_manage_engagement" +
                "&response_type=token";
        return new RedirectView(authUrl);
    }

    @GetMapping("/callback")
    public RedirectView facebookCallback(@RequestParam("code") String code) {
        String tokenUrl = "https://graph.facebook.com/v19.0/oauth/access_token" +
                "?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=http://localhost:8080/api/v1/account/facebook/home" +
                "&code=" + code;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", "http://localhost:8080/api/v1/account/facebook/home");
        params.put("code", code);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, null, String.class, params);
        // Instead of returning raw JSON, redirect to frontend with token
        return new RedirectView("http://localhost:8080/api/v1/account/facebook/home?token=" + response.getBody());
    }


    @PostMapping("/long-lived-token")
    public String getLongLivedToken(String shortLivedToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/v19.0/oauth/access_token" +
                "?grant_type=fb_exchange_token" +
                "?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&fb_exchange_token=" + shortLivedToken;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    @PostMapping("/post-on-page")
    public String postOnPage(@RequestParam String pageAccessToken,
                             @RequestParam String pageId,
                             @RequestParam String message) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/" + pageId + "/feed";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("message", message);
        body.add("access_token", pageAccessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    @GetMapping("/page-insights")
    public String getPageInsights(String pageId, String pageAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graph.facebook.com/" + pageId + "/insights?metric=page_impressions,page_engaged_users" +
                "&access_token=" + pageAccessToken;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    @GetMapping("redirect")
    public ResponseEntity<String> redirect() {
        return ResponseEntity.ok("Redirected Successfully");
    }
/*
    @GetMapping("/home")
    @ResponseBody
    public String home(@AuthenticationPrincipal OAuth2User user, @RequestParam(name = "token") String token) {
        return "Welcome, " + user.getAttribute("name") + "!" + "here's your token: " + token;
    }*/
}
