package com.MarketingMVP.AllVantage.DTOs.Authentication.Employee;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeLoginResponseDTO {
    private EmployeeDTO employeeDTO;
    private String accessToken;
    private String refreshToken;
}
