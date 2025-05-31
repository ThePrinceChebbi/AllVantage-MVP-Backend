package com.MarketingMVP.AllVantage.Services.UserEntity;



import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Admin;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

    UserEntity getUserById(final UUID userId);
    Client getClientById(final UUID userId);
    Employee getEmployeeById(final UUID userId);
    Admin getAdminById(final UUID userId);
    UserEntity getUserByUsername(final String username);
    Employee getEmployeeByUsername(final String username);
    boolean isEmailRegistered(final String email);
    boolean isUsernameRegistered(final String username);
    boolean isPhoneNumberRegistered(final String phoneNumber);
    Employee saveEmployee(@NonNull final Employee employee);
    Client saveClient(@NonNull final Client client);
    <T extends UserEntity> T saveUser(final T userEntity);
    ResponseEntity<Object> lockAccount(UUID id, UserDetails userDetails);
    ResponseEntity<Object> disableAccount(LoginDTO loginDTO);
    UserEntity enableAccount(UUID id);
    ResponseEntity<Object> unlockAccount(UUID id, UserDetails userDetails);

    ResponseEntity<Object> getAllUsers();

    boolean clientExists(UUID clientId);

    ResponseEntity<Object> addImage(MultipartFile file, UUID id);

    ResponseEntity<Object> getAllClients(int pageNumber);

    ResponseEntity<Object> getAllEmployees(int pageNumber);

    ResponseEntity<Object> getAccountById(UUID id);

    ResponseEntity<Object> updateUserInfo(UUID id, UserEntity userEntity, UserDetails userDetails);
}
