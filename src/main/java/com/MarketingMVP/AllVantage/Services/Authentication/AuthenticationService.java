package com.MarketingMVP.AllVantage.Services.Authentication;

import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
     ResponseEntity<Object> login(LoginDTO loginDTO);
     String confirmation(String ConfirmationToken);
     ResponseEntity<Object> createClientAccount(ClientRegisterDTO clientRegisterDTO);
     ResponseEntity<Object> createEmployeeAccount(EmployeeRegisterDTO employeeRegisterDTO);
     ResponseEntity<Object> refresh(String refreshToken, String expiredToken);

}
