package com.MarketingMVP.AllVantage.DTOs.Authentication.Employee;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.EmployeeDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeRegisterResponseDTO {
    private EmployeeDTO employeeDTO;
    private String confirmationToken;
    private String refreshToken;

    public EmployeeRegisterResponseDTO(EmployeeDTO employeeDTO, String confirmationToken, String refreshToken) {
        this.employeeDTO = employeeDTO;
        this.confirmationToken = confirmationToken;
        this.refreshToken = refreshToken;
    }
}
