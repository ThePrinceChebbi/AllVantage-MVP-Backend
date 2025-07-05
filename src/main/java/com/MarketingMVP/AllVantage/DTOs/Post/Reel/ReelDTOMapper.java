package com.MarketingMVP.AllVantage.DTOs.Post.Reel;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Postable.Reel.Reel;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ReelDTOMapper implements Function<Reel, ReelDTO> {

    private final UserService userService;

    public ReelDTOMapper(UserService userService) {
        this.userService = userService;
    }
    @Override
    public ReelDTO apply(Reel reel) {
        return new ReelDTO(
                reel.getId(),
                reel.getTitle(),
                reel.getContent(),
                reel.getCreatedAt(),
                reel.getScheduledToPostAt(),
                reel.getLastEditedAt(),
                reel.getEmployee() != null ? new UserDTOMapper().apply(reel.getEmployee()) :
                        new UserDTOMapper().apply(userService.getUserByUsername("testemp123")),
                reel.getFacebookReels(),
                reel.getInstagramReels(),
                reel.getLinkedinReels()
        );
    }
}
