package com.MarketingMVP.AllVantage.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class RedirectTestController {
    @GetMapping("/")
    public String redirect() {
        return "Redirected Successfully";
    }
}
