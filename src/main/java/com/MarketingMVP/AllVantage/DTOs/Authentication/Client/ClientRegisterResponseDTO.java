package com.MarketingMVP.AllVantage.DTOs.Authentication.Client;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientRegisterResponseDTO {
    private ClientDTO clientDTO;
    private String confirmationToken;
    private String refreshToken;

    public ClientRegisterResponseDTO(ClientDTO clientDTO, String confirmationToken, String refreshToken) {
        this.clientDTO = clientDTO;
        this.confirmationToken = confirmationToken;
        this.refreshToken = refreshToken;
    }
}
