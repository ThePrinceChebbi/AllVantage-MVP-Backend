package com.MarketingMVP.AllVantage.Services.Authentication;


import com.MarketingMVP.AllVantage.DTOs.Authentication.*;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientLogInResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeLoginResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.EmployeeDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.Token;
import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.TokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken.RefreshToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Mail.SimpleMailSender;
import com.MarketingMVP.AllVantage.Security.JWT.JWTService;
import com.MarketingMVP.AllVantage.Services.Role.RoleService;
import com.MarketingMVP.AllVantage.Services.Token.ConfirmationTokenService;
import com.MarketingMVP.AllVantage.Services.Token.RefreshTokenService;
import com.MarketingMVP.AllVantage.Services.Token.TokenService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import io.micrometer.common.lang.NonNull;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final SimpleMailSender simpleMailSender;
    private final EmployeeDTOMapper employeeDTOMapper;
    private final ClientDTOMapper clientDTOMapper;

    public AuthenticationServiceImpl(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder, ConfirmationTokenService confirmationTokenService, RefreshTokenService refreshTokenService, TokenService tokenService, AuthenticationManager authenticationManager, JWTService jwtService, SimpleMailSender simpleMailSender, EmployeeDTOMapper employeeDTOMapper, ClientDTOMapper clientDTOMapper) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.simpleMailSender = simpleMailSender;
        this.employeeDTOMapper = employeeDTOMapper;
        this.clientDTOMapper = clientDTOMapper;
    }

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public ResponseEntity<Object> createClientAccount(ClientRegisterDTO clientRegisterDTO) {
        try {
            if (userService.isEmailRegistered(clientRegisterDTO.getEmail())) {
                throw new IllegalArgumentException("Sorry, that email is already taken. Please choose a different one.");
            }

            if (userService.isPhoneNumberRegistered(clientRegisterDTO.getPhoneNumber())) {
                throw new IllegalArgumentException("Sorry, that phone number is already taken. Please choose a different one.");
            }
            Role role = roleService.fetchRoleByName("CLIENT");
            Client client = new Client();
            client.setFirstName(clientRegisterDTO.getName());
            client.setEmail(clientRegisterDTO.getEmail().toLowerCase());
            client.setPhoneNumber(clientRegisterDTO.getPhoneNumber());
            client.setCreationDate(new Date());
            client.setPassword(passwordEncoder.encode(clientRegisterDTO.getPassword()));
            client.setLocked(true);
            client.setRole(role);

            Client saveClient = userService.saveClient(client);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(saveClient);
            String refreshToken = refreshTokenService.generateRefreshToken(saveClient);
            String link = baseUrl + "/api/v1/auth/confirm?token=" + confirmationToken;
            simpleMailSender.sendAuthMailToClient(saveClient, "Confirmation email", link);

            final ClientRegisterResponseDTO registerResponse = ClientRegisterResponseDTO
                    .builder()
                    .confirmationToken(confirmationToken)
                    .refreshToken(refreshToken)
                    .clientDTO(clientDTOMapper.apply(saveClient))
                    .build();

            return ResponseEntity.status(200).body(registerResponse);
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> createEmployeeAccount(EmployeeRegisterDTO employeeRegisterDTO) {
        try {
            if (userService.isUsernameRegistered(employeeRegisterDTO.getUsername())) {
                throw new IllegalArgumentException("Sorry, that email is already taken. Please choose a different one.");
            }
            if (userService.isEmailRegistered(employeeRegisterDTO.getEmail())) {
                throw new IllegalArgumentException("Sorry, that email is already taken. Please choose a different one.");
            }

            if (userService.isPhoneNumberRegistered(employeeRegisterDTO.getPhoneNumber())) {
                throw new IllegalArgumentException("Sorry, that phone number is already taken. Please choose a different one.");
            }
            Role role = roleService.fetchRoleByName("CLIENT");
            Employee employee = new Employee();
            employee.setFirstName(employeeRegisterDTO.getFirstName());
            employee.setEmail(employeeRegisterDTO.getEmail().toLowerCase());
            employee.setPhoneNumber(employeeRegisterDTO.getPhoneNumber());
            employee.setCreationDate(new Date());
            employee.setPassword(passwordEncoder.encode(employeeRegisterDTO.getPassword()));
            employee.setLocked(true);
            employee.setRole(role);

            Employee savedEmployee = userService.saveEmployee(employee);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(savedEmployee);
            String refreshToken = refreshTokenService.generateRefreshToken(savedEmployee);
            String link = baseUrl + "/api/v1/auth/confirm?token=" + confirmationToken;
            simpleMailSender.sendAuthMailToEmployee(savedEmployee, "Confirmation email", link);

            final EmployeeRegisterResponseDTO registerResponse = EmployeeRegisterResponseDTO
                    .builder()
                    .confirmationToken(confirmationToken)
                    .refreshToken(refreshToken)
                    .employeeDTO(employeeDTOMapper.apply(savedEmployee))
                    .build();

            return ResponseEntity.status(200).body(registerResponse);
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> login(@NonNull LoginDTO loginDto) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserEntity user = userService.getUserByEmail(loginDto.getEmail());
            revokeAllUserAccessTokens(user);
            revokeAllUserRefreshToken(user);
            String jwtAccessToken = revokeGenerateAndSaveToken(user);
            String jwtRefreshToken = refreshTokenService.generateRefreshToken(user);

            if (user.getRole().getName().equals("CLIENT")) {
                final ClientLogInResponseDTO logInResponse = ClientLogInResponseDTO
                        .builder()
                        .accessToken(jwtAccessToken)
                        .refreshToken(jwtRefreshToken)
                        .clientDTO(clientDTOMapper.apply(userService.getClientById(user.getId())))
                        .build();
                return ResponseEntity.status(200).body(logInResponse);
            }else if (user.getRole().getName().equals("EMPLOYEE")) {
                final EmployeeLoginResponseDTO logInResponse = EmployeeLoginResponseDTO
                        .builder()
                        .accessToken(jwtAccessToken)
                        .refreshToken(jwtRefreshToken)
                        .build();
                return ResponseEntity.status(200).body(logInResponse);
            }else {
                final EmployeeLoginResponseDTO logInResponse = EmployeeLoginResponseDTO
                        .builder()
                        .accessToken(jwtAccessToken)
                        .refreshToken(jwtRefreshToken)
                        .employeeDTO(employeeDTOMapper.apply(userService.getEmployeeById(user.getId())))
                        .build();
                return ResponseEntity.status(200).body(logInResponse);
            }
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public String confirmation(String token) {
        try{
            ConfirmationToken confirmationToken = confirmationTokenService.fetchTokenByToken(token);
            if (confirmationToken.getConfirmationDate() != null) {
                return confirmationTokenService.getAlreadyConfirmedPage();
            }
            LocalDateTime expiredAt = confirmationToken.getExpirationDate();

            if (expiredAt.isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Confirmation token expired.");
            }

            UserEntity userEntity = userService.enableAccount(confirmationToken.getUserEntity().getId());

            confirmationTokenService.setConfirmedAt(confirmationToken);
            return confirmationTokenService.getConfirmationPage();
        } catch (Exception e){
            return e.getMessage();
        }
    }

    @Override
    public ResponseEntity<Object> register(@NonNull EmployeeRegisterDTO personRegisterDTO) {
        try {
            if (userService.isEmailRegistered(personRegisterDTO.getEmail())) {
                throw new IllegalArgumentException("Sorry, that email is already taken. Please choose a different one.");
            }
            if (userService.isPhoneNumberRegistered(personRegisterDTO.getPhoneNumber())) {
                throw new IllegalArgumentException("Sorry, that phone number is already taken. Please choose a different one.");
            }
            Role role = roleService.fetchRoleByName("CLIENT");
            Employee person = new Employee();
            person.setFirstName(personRegisterDTO.getFirstName());
            person.setLastName(personRegisterDTO.getLastName());
            person.setEmail(personRegisterDTO.getEmail().toLowerCase());
            person.setPhoneNumber(personRegisterDTO.getPhoneNumber());
            person.setCreationDate(new Date());
            person.setPassword(passwordEncoder.encode(personRegisterDTO.getPassword()));
            person.setLocked(true);
            person.setEnabled(false);
            person.setRole(role);

            Employee savedEmployee = userService.saveEmployee(person);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(savedEmployee);
            String refreshToken = refreshTokenService.generateRefreshToken(savedEmployee);
            String link = "http://localhost:8080/api/v1/auth/confirm?token=" + confirmationToken;
            simpleMailSender.sendAuthMailToEmployee(savedEmployee, "Confirmation email", link);

            final EmployeeRegisterResponseDTO registerResponse = EmployeeRegisterResponseDTO
                    .builder()
                    .confirmationToken(confirmationToken)
                    .refreshToken(refreshToken)
                    .employeeDTO(employeeDTOMapper.apply(savedEmployee))
                    .build();

            return ResponseEntity.status(200).body(registerResponse);
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }    }


    @Override
    public ResponseEntity<Object> refresh(final String refreshToken, final String expiredToken) {
        try{
            final Token currentToken = tokenService.getTokenByToken(expiredToken);
            final UserEntity currentUser = currentToken.getUserEntity();
            final RefreshToken currentRefreshToken = refreshTokenService.fetchRefreshTokenByToken(refreshToken);
            final boolean isRefreshTokenValid = refreshTokenService.validateRefreshToken(refreshToken);
            if (currentRefreshToken.getUserEntity().getId() != currentUser.getId()) {
                throw new IllegalStateException("The access token and refresh token u provided are not compatible.");
            }
            revokeAllUserAccessTokens(currentUser);
            String jwtAccessToken = revokeGenerateAndSaveToken(currentUser);
            final RefreshTokenResponseDTO refreshTokenResponse = RefreshTokenResponseDTO
                    .builder()
                    .accessToken(jwtAccessToken)
                    .refreshToken(refreshToken)
                    .build();
            return ResponseEntity.status(200).body(refreshTokenResponse);
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }



    private String revokeGenerateAndSaveToken(UserEntity user) {
        String jwtToken = jwtService.generateToken(user);
        revokeAllUserAccessTokens(user);
        saveUserAccessToken(user, jwtToken);
        return jwtToken;
    }

    private void saveUserAccessToken(@NonNull UserEntity userEntity, @NonNull String jwtToken) {
        var token = new Token(
                jwtToken,
                TokenType.BEARER,
                false,
                false,
                userEntity
        );
        tokenService.save(token);
    }

    private void revokeAllUserAccessTokens(@NotNull UserEntity userEntity) {
        var validUserTokens = tokenService.fetchAllValidTokenByUserId(userEntity.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenService.saveAll(validUserTokens);
    }

    private void revokeAllUserRefreshToken(@NotNull UserEntity userEntity) {
        var validRefreshTokens = refreshTokenService.fetchAllRefreshTokenByUserId(userEntity.getId());
        if (validRefreshTokens.isEmpty()) {
            return;
        }
        validRefreshTokens.forEach(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshToken.setExpired(true);
        });
        refreshTokenService.saveAll(validRefreshTokens);
    }

}
