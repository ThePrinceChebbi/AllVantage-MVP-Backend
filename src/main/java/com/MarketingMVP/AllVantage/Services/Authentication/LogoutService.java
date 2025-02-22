package com.MarketingMVP.AllVantage.Services.Authentication;

import com.MarketingMVP.AllVantage.Services.Token.Access.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {

    private final TokenService tokenService;

    public LogoutService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){ return;}
        final String jwt = authHeader.substring(7);
        var storedToken = tokenService.getTokenByToken(jwt);
        if(storedToken != null) {

            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenService.save(storedToken);

        }

    }
}
