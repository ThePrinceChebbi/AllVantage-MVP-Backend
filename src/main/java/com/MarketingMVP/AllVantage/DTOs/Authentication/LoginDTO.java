package com.MarketingMVP.AllVantage.DTOs.Authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDTO {

    private String username;

    private String password;

    private boolean rememberMe;

}
