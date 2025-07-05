package com.MarketingMVP.AllVantage.DTOs.Post.Post;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Postable.Post.Post;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class PostDTOMapper implements Function<Post, PostDTO> {

    private final UserService userService;

    public PostDTOMapper(UserService userService) {
        this.userService = userService;
    }

    @Override
    public PostDTO apply(Post post) {
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getScheduledToPostAt(),
                post.getLastEditedAt(),
                post.getEmployee() != null ? new UserDTOMapper().apply(post.getEmployee()) :
                        new UserDTOMapper().apply(userService.getUserByUsername("testemp123")),
                post.getFacebookPosts(),
                post.getInstagramPosts(),
                post.getLinkedinPosts()
        );
    }
}
