package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Instagram.InstagramAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization.LinkedInOrganizationDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class SuitDTOMapper implements Function<Suit, SuitDTO> {

    private final UserDTOMapper userDTOMapper;

    public SuitDTOMapper(UserDTOMapper userDTOMapper) {
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public SuitDTO apply(Suit suit) {
        return new SuitDTO(
                suit.getId(),
                suit.getName(),
                suit.getDescription(),
                "http://localhost:8080/api/v1/files/" + suit.getImage().getId(),
                suit.getSuitColor(),
                suit.isActive(),
                userDTOMapper.apply(suit.getClient()),
                suit.getEmployees().stream().map(userDTOMapper).toList(),
                suit.getFacebookPages().stream().map(new FacebookPageDTOMapper()).toList(),
                suit.getInstagramAccounts().stream().map(new InstagramAccountDTOMapper()).toList(),
                suit.getLinkedInOrganizations().stream().map(new LinkedInOrganizationDTOMapper()).toList()
        );
    }
}
