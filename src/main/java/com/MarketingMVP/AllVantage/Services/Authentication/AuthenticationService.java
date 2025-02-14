package com.MarketingMVP.AllVantage.Services.Authentication;

import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
     ResponseEntity<Object> login(LoginDTO loginDTO);
     String confirmation(String ConfirmationToken);
     ResponseEntity<Object> register(@NotNull final EmployeeRegisterDTO employeeRegisterDTO) ;
     ResponseEntity<Object> createClientAccount(ClientRegisterDTO clientRegisterDTO);
     ResponseEntity<Object> createEmployeeAccount(EmployeeRegisterDTO employeeRegisterDTO);
     ResponseEntity<Object> refresh(String token, String expiredToken);

}
