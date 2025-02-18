package com.MarketingMVP.AllVantage.DTOs.Authentication.Client;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientLogInResponseDTO {
    private ClientDTO clientDTO;
    private String accessToken;
    private String refreshToken;
}
