package com.MarketingMVP.AllVantage.Services.UserEntity;


import com.MarketingMVP.AllVantage.DTOs.Authentication.LoginDTO;
import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Admin.AdminDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Admin;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Exceptions.UnauthorizedActionException;
import com.MarketingMVP.AllVantage.Repositories.UserEntity.UserRepository;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmployeeDTOMapper employeeDTOMapper;
    private final ClientDTOMapper clientDTOMapper;
    private final AdminDTOMapper adminDTOMapper;
    private final FileService fileService;
    private final UserDTOMapper userDTOMapper;

    public UserServiceImpl(UserRepository userRepository, EmployeeDTOMapper employeeDTOMapper, ClientDTOMapper clientDTOMapper, AdminDTOMapper adminDTOMapper, FileService fileService, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.employeeDTOMapper = employeeDTOMapper;
        this.clientDTOMapper = clientDTOMapper;
        this.adminDTOMapper = adminDTOMapper;
        this.fileService = fileService;
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public ResponseEntity<Object> unlockAccount(UUID id, UserDetails userDetails) {
        try{
            UserEntity locker = getUserByUsername(userDetails.getUsername());
            if (!locker.getRole().getName().equals("ADMIN")){
                throw new UnauthorizedActionException("Sorry, you can't lock an account");
            }
            UserEntity user = getUserById(id);
            if (Objects.equals(user.getRole().getName(), "ADMIN")){
                throw new UnauthorizedActionException("Sorry, you can't lock an Admin");
            }
            user.setLocked(false);
            return ResponseEntity.status(200).body(userDTOMapper.apply(saveUser(user)));
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }

    }

    @Override
    public ResponseEntity<Object> getAllUsers() {
        List<Record> users = userRepository.findAll().stream().map(user -> {
            if (user instanceof Employee){
                return employeeDTOMapper.apply((Employee) user);
            }else if (user instanceof Client){
                return clientDTOMapper.apply((Client) user);
            }else {
                return adminDTOMapper.apply((Admin) user);
            }
        }).toList();
        return ResponseEntity.status(200).body(users);
    }

    @Override
    public boolean clientExists(UUID clientId) {
        return userRepository.clientExists(clientId);
    }

    @Override
    public ResponseEntity<Object> addImage(MultipartFile file, UUID id) {
        try{
            UserEntity user = getUserById(id);
            FileData fileData = fileService.processUploadedFile(file);
            user.setImage(fileData);
            saveUser(user);
            return ResponseEntity.status(200).body("Image added successfully");
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAllClients(int pageNumber) {
        try {
            int size = 10;
            int total = userRepository.getTotalClientCount();
            if (total == 0) {
                return ResponseEntity.status(200).body("No clients found");
            }

            int totalPages = (int) Math.ceil((double) total / size);
            if (pageNumber < 0 || pageNumber >= totalPages) {
                pageNumber = 0;
            }

            Pageable pageable = PageRequest.of(pageNumber, size);
            List<ClientDTO> clients = userRepository.findAllClients(pageable).stream().map(clientDTOMapper).toList();

            Map<String, Object> map = new HashMap<>();
            map.put("clients", clients);
            map.put("pageNumber", pageNumber);
            map.put("pageSize", clients.size());
            map.put("total", total);

            return ResponseEntity.status(200).body(map);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAllEmployees(int pageNumber) {
        try{
            int size = 10;
            int total = userRepository.getTotalEmployeeCount();
            if (total == 0){
                return ResponseEntity.status(200).body("No employees found");
            }
            int totalPages = (int) Math.ceil((double) total / size);
            if (pageNumber < 0 || pageNumber >= totalPages){
                pageNumber = 0;
            }
            Pageable pageable = PageRequest.of(pageNumber, size);
            List<EmployeeDTO> employees = userRepository.findAllEmployees(pageable).stream().map(employeeDTOMapper).toList();

            Map<String, Object> map = new HashMap<>();
            map.put("employees", employees);
            map.put("pageNumber", pageNumber);
            map.put("pageSize", employees.size());
            map.put("total", total);
            return ResponseEntity.status(200).body(map);
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> getAccountById(UUID id) {
        try{
            UserEntity user = getUserById(id);
            if (user instanceof Employee){
                Map<String, Object> map = new HashMap<>();
                map.put("user", userDTOMapper.apply(user));
                map.put("suits", ((Employee) user).getSuits().stream().map(new SuitDTOMapper(clientDTOMapper, employeeDTOMapper)).toList());
                return ResponseEntity.status(200).body(map);
            }else if (user instanceof Client){
                Map<String, Object> map = new HashMap<>();
                map.put("user", userDTOMapper.apply(user));
                map.put("suits", ((Client) user).getSuits().stream().map(new SuitDTOMapper(clientDTOMapper, employeeDTOMapper)).toList());
                return ResponseEntity.status(200).body(map);
            }else {
                throw new UnauthorizedActionException("Sorry you can't access this account");
            }
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> updateUserInfo(UUID id, UserEntity userEntity, UserDetails userDetails) {
        try{
            UserEntity user = getUserById(id);
            UserEntity changer = getUserByUsername(userDetails.getUsername());
            if(userDetails.getUsername().equals(user.getUsername()) || changer.getRole().getName().equals("ADMIN")){
                user.setFirstName(userEntity.getFirstName());
                user.setLastName(userEntity.getLastName());
                user.setPhoneNumber(userEntity.getPhoneNumber());
                user.setEmail(userEntity.getEmail());
                user.setCountry(userEntity.getCountry());
                user.setState(userEntity.getState());
                user.setPostalCode(userEntity.getPostalCode());
                user.setAddress(userEntity.getAddress());
                UserEntity savedUser = saveUser(user);
                return ResponseEntity.status(200).body(userDTOMapper.apply(savedUser));
            } else {
                throw new UnauthorizedActionException("Sorry you can't access this account");
            }
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (UnauthorizedActionException e){
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> lockAccount(UUID id,UserDetails userDetails) {
        try{
            UserEntity locker = getUserByUsername(userDetails.getUsername());
            if (!locker.getRole().getName().equals("ADMIN")){
                throw new UnauthorizedActionException("Sorry, you can't lock an account");
            }
            UserEntity user = getUserById(id);
            if (Objects.equals(user.getRole().getName(), "ADMIN")){
                throw new UnauthorizedActionException("Sorry, you can't lock an Admin");
            }
            user.setLocked(true);
            saveUser(user);
            return ResponseEntity.status(200).body(userDTOMapper.apply(saveUser(user)));
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
            UserEntity user = getUserByUsername(loginDTO.getUsername());
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
    public UserEntity getUserByUsername(@NonNull final String username) throws UnauthorizedActionException{
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The user with username : %s could not be found.", username)));
    }

    @Override
    public Employee getEmployeeByUsername(String username) {
        return userRepository.fetchEmployeeWithUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The Employee with username : %s could not be found.", username)));
    }
}
