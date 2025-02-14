package com.MarketingMVP.AllVantage.Services.UserEntity;



import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserService {
    ResponseEntity<Object> fetchAgencyById(final UUID userId);
    ResponseEntity<Object> fetchPersonById(final UUID userId);
    ResponseEntity<Object> fetchAllAgencies();
    ResponseEntity<Object> fetchAllPeople();
    ResponseEntity<Object> fetchCurrentUser(final UserDetails userDetails);

    UserEntity getUserById(final UUID userId);
    Client getClientById(final UUID userId);
    Employee getEmployeeById(final UUID userId);
    UserEntity getUserByEmail(final String email);
    boolean isEmailRegistered(final String email);
    boolean isUsernameRegistered(final String username);
    boolean isPhoneNumberRegistered(final String phoneNumber);
    Employee saveEmployee(@NonNull final Employee employee);
    Client saveClient(@NonNull final Client client);
    UserEntity saveUser(@NonNull UserEntity userEntity);
    ResponseEntity<Object> lockAccount(UUID id);
    ResponseEntity<Object> disableAccount(LoginDTO loginDTO);
    UserEntity enableAccount(UUID id);
    ResponseEntity<Object> unlockAccount(UUID id);

}
