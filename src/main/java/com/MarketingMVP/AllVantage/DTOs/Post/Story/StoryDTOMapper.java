package com.MarketingMVP.AllVantage.DTOs.Post.Story;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Postable.Reel.Reel;
import com.MarketingMVP.AllVantage.Entities.Postable.Story.Story;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class StoryDTOMapper implements Function<Story, StoryDTO> {

    private final UserService userService;

    public StoryDTOMapper(UserService userService) {
        this.userService = userService;
    }

    @Override
    public StoryDTO apply(Story reel) {
        return new StoryDTO(
                reel.getId(),
                reel.getTitle(),
                reel.getCreatedAt(),
                reel.getScheduledToPostAt(),
                reel.getLastEditedAt(),
                reel.getEmployee() != null ? new UserDTOMapper().apply(reel.getEmployee()) :
                        new UserDTOMapper().apply(userService.getUserByUsername("testemp123")),
                reel.getFacebookStories(),
                reel.getInstagramStories()
        );
    }
}
