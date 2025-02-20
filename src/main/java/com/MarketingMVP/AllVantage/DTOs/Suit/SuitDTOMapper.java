package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
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
                suit.getImage(),
                clientDTOMapper.apply(suit.getClient()),
                suit.getEmployees().stream().map(employeeDTOMapper).toList(),
                suit.getFacebookPages(),
                suit.getInstagramAccounts(),
                suit.getLinkedInAccounts(),
                suit.getXAccounts(),
                suit.getSnapchatAccounts(),
                suit.getTikTokAccounts()
        );
    }
}
