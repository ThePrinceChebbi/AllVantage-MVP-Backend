package com.MarketingMVP.AllVantage.Security.JWT;

import com.MarketingMVP.AllVantage.Services.Authentication.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class CustomLogoutHandler implements LogoutHandler {

    private final LogoutService logoutService;

    public CustomLogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutService.logout(request,response,authentication);
    }
}
