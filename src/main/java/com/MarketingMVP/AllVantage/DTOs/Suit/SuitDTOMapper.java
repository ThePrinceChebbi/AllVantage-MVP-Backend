package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.Facebook.Page.FacebookPageDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.Instagram.InstagramAccountDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.Organization.LinkedInOrganizationDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class SuitDTOMapper implements Function<Suit, SuitDTO> {

    private final ClientDTOMapper clientDTOMapper;
    private final EmployeeDTOMapper employeeDTOMapper;

    public SuitDTOMapper(ClientDTOMapper clientDTOMapper, EmployeeDTOMapper employeeDTOMapper) {
        this.clientDTOMapper = clientDTOMapper;
        this.employeeDTOMapper = employeeDTOMapper;
    }

    @Override
    public SuitDTO apply(Suit suit) {
        return new SuitDTO(
                suit.getId(),
                suit.getName(),
                suit.getDescription(),
                "http://localhost:8080/api/v1/files/" + suit.getImage().getId(),
                suit.getSuitColor(),
                clientDTOMapper.apply(suit.getClient()),
                suit.getEmployees().stream().map(employeeDTOMapper).toList(),
                suit.getFacebookPages().stream().map(new FacebookPageDTOMapper()).toList(),
                suit.getInstagramAccounts().stream().map(new InstagramAccountDTOMapper()).toList(),
                suit.getLinkedInOrganizations().stream().map(new LinkedInOrganizationDTOMapper()).toList()
        );
    }
}
