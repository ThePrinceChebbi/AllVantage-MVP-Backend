package com.MarketingMVP.AllVantage.DTOs.Authentication.Employee;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeRegisterResponseDTO {
    private UserDTO userDTO;
    private String confirmationToken;

    public EmployeeRegisterResponseDTO(UserDTO userDTO, String confirmationToken) {
        this.userDTO = userDTO;
        this.confirmationToken = confirmationToken;
    }
}
