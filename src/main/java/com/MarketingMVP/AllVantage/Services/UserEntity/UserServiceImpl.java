package com.MarketingMVP.AllVantage.Services.UserEntity;


import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTOMapper;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Admin;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Exceptions.UnauthorizedActionException;
import com.MarketingMVP.AllVantage.Repositories.UserEntity.UserRepository;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmployeeDTOMapper employeeDTOMapper;
    private final ClientDTOMapper clientDTOMapper;

    public UserServiceImpl(UserRepository userRepository, EmployeeDTOMapper employeeDTOMapper, ClientDTOMapper clientDTOMapper) {
        this.userRepository = userRepository;
        this.employeeDTOMapper = employeeDTOMapper;
        this.clientDTOMapper = clientDTOMapper;
    }

    @Override
    public ResponseEntity<Object> unlockAccount(UUID id) {
        try{
            UserEntity user = getUserById(id);
            user.setLocked(true);
            saveUser(user);
            return ResponseEntity.status(200).body("Account has been enabled successfully");
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> lockAccount(UUID id) {
        try{
            UserEntity user = getUserById(id);
            if (Objects.equals(user.getRole().getName(), "ADMIN")){
                throw new UnauthorizedActionException("Sorry, you can't lock an Admin");
            }
            user.setLocked(false);
            saveUser(user);
            return ResponseEntity.status(200).body("Account locked successfully");
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (UnauthorizedActionException e){
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> disableAccount(LoginDTO loginDTO) {
        try{
            UserEntity user = getUserByEmail(loginDTO.getEmail());
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(passwordEncoder.encode(loginDTO.getPassword()), user.getPassword())){
                user.setEnabled(false);
                saveUser(user);
            }else {
                throw new UnauthorizedActionException("Wrong password");
            }
            return ResponseEntity.status(200).body("Account disabled successfully");
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (UnauthorizedActionException e){
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public UserEntity enableAccount(UUID id) {
        UserEntity user = getUserById(id);
        user.setEnabled(true);
        return saveUser(user);
    }


    @Override
    public boolean isEmailRegistered(final String email) {
        return userRepository.isEmailRegistered(email);
    }

    @Override
    public boolean isUsernameRegistered(String username) {
        return userRepository.isUsernameRegistered(username);
    }

    @Override
    public boolean isPhoneNumberRegistered(final String phoneNumber) {
        return userRepository.isPhoneNumberRegistered(phoneNumber);
    }

    @Override
    public Client saveClient(final Client client) {
        return userRepository.save(client);
    }

    public <T extends UserEntity> T saveUser(final T userEntity) {
        return userRepository.save(userEntity);
    }

    @Override
    public Employee saveEmployee(final Employee employee) {
        return userRepository.save(employee);
    }

    @Override
    public UserEntity getUserById(final UUID userId) {
        return userRepository.fetchUserWithId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The user with ID : %s could not be found.", userId)));
    }

    @Override
    public Client getClientById(UUID userId) {
        return userRepository.getClientById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The Client with ID : %s could not be found.", userId)));
    }

    @Override
    public Employee getEmployeeById(UUID userId) {
        return userRepository.getEmployeeById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The Employee with ID : %s could not be found.", userId)));
    }

    @Override
    public Admin getAdminById(UUID userId) {
        return userRepository.getAdminById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The Admin with ID : %s could not be found.", userId)));
    }

    @Override
    public UserEntity getUserByEmail(@NonNull final String userEmail) throws UnauthorizedActionException{
        return userRepository.fetchUserWithEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The user with email : %s could not be found.", userEmail)));
    }
}
