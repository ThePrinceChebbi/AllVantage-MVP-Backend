package com.MarketingMVP.AllVantage.DTOs.Authentication.Client;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ClientRegisterResponseDTO {
    private UserDTO userDTO;
    private String confirmationToken;

    public ClientRegisterResponseDTO(UserDTO userDTO, String confirmationToken) {
        this.userDTO = userDTO;
        this.confirmationToken = confirmationToken;
    }
}
