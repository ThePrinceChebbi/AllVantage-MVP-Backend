package com.MarketingMVP.AllVantage.Controllers.Authetication;

import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.Services.Authentication.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO) {
        return authenticationService.login(loginDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestParam(name = "refreshToken") String refreshToken, @RequestParam(name = "expiredToken") String expiredToken) {
        return authenticationService.refresh(refreshToken, expiredToken);
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam(name = "token") String token) {
        return authenticationService.confirmation(token);
    }
}
