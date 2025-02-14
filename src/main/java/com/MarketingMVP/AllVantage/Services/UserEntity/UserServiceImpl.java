package com.MarketingMVP.AllVantage.Services.UserEntity;


import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.EmployeeDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.EmployeeDTOMapper;
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
    public ResponseEntity<Object> fetchPersonById(final UUID userId) {
        final EmployeeDTO personDTO = employeeDTOMapper.apply(getEmployeeById(userId));

        return ResponseEntity.status(200).body(personDTO);
    }
    @Override
    public ResponseEntity<Object> fetchAgencyById(final UUID userId) {
        final ClientDTO agencyDTO = clientDTOMapper.apply(getClientById(userId));

        return ResponseEntity.status(200).body(agencyDTO);
    }

    @Override
    public ResponseEntity<Object> fetchAllPeople() {
        final List<EmployeeDTO> personDTOList = userRepository.findAllPeople().stream().map(employeeDTOMapper).toList();
        return ResponseEntity.status(200).body(personDTOList);
    }
    @Override
    public ResponseEntity<Object> fetchAllAgencies() {
        final List<ClientDTO> agencyDTOList= userRepository.findAllAgencies().stream().map(clientDTOMapper).toList();
        return ResponseEntity.status(200).body(agencyDTOList);
    }

    @Override
    public ResponseEntity<Object> fetchCurrentUser(@NonNull final UserDetails userDetails) {

        final UserEntity currentUser = getUserByEmail(userDetails.getUsername());
        if (currentUser.getRole().getName().equals("CLIENT")) {
            ClientDTO currentUserDto = clientDTOMapper.apply(getClientById(currentUser.getId()));
            return ResponseEntity.status(200).body(currentUserDto);
        }else {
            EmployeeDTO currentUserDto = employeeDTOMapper.apply(getEmployeeById(currentUser.getId()));
            return ResponseEntity.status(200).body(currentUserDto);
        }
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

    public UserEntity saveUser(final UserEntity userEntity) {
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
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The Agency with ID : %s could not be found.", userId)));
    }

    @Override
    public Employee getEmployeeById(UUID userId) {
        return userRepository.getEmployeeById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The Person with ID : %s could not be found.", userId)));
    }

    @Override
    public UserEntity getUserByEmail(@NonNull final String userEmail) throws UnauthorizedActionException{
        return userRepository.fetchUserWithEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The user with email : %s could not be found.", userEmail)));
    }
}
