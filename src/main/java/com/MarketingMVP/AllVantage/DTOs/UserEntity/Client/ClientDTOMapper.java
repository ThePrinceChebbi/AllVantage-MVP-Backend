package com.MarketingMVP.AllVantage.DTOs.UserEntity.Client;

import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ClientDTOMapper implements Function<Client, ClientDTO> {
    @Override
    public ClientDTO apply(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getUsername(),
                client.getEmail(),
                "http://localhost:8080/api/v1/files/" + client.getImage().getId(),
                client.getPhoneNumber(),
                client.getRole(),
                client.getCreationDate(),
                client.isEnabled(),
                client.isLocked()
        );
    }
}
