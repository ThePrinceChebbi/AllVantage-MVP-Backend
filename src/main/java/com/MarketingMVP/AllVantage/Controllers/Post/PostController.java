package com.MarketingMVP.AllVantage.Controllers.Post;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {
    @GetMapping("/test")
    public String test() {
        System.out.println("Post Controller Test");
        return "Post Controller Test";
    }
}
