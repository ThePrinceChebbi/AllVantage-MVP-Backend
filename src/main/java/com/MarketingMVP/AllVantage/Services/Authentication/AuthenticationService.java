package com.MarketingMVP.AllVantage.Services.Authentication;

import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
     ResponseEntity<Object> login(LoginDTO loginDTO);
     String confirmation(String ConfirmationToken);
     ResponseEntity<Object> createClientAccount(ClientRegisterDTO clientRegisterDTO);
     ResponseEntity<Object> createEmployeeAccount(EmployeeRegisterDTO employeeRegisterDTO);
     ResponseEntity<Object> refresh(HttpServletRequest request, HttpServletResponse response);

     ResponseEntity<UserDTO> getMe(UserDetails userDetails);

}
