package com.MarketingMVP.AllVantage.Services.Authentication;


import com.MarketingMVP.AllVantage.DTOs.Authentication.*;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Admin.AdminLoginResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientLogInResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeLoginResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Admin.AdminDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTOMapper;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee.EmployeeDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.Token;
import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.TokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken.RefreshToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Security.JWT.JWTService;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.Role.RoleService;
import com.MarketingMVP.AllVantage.Services.Token.Confirmation.ConfirmationTokenService;
import com.MarketingMVP.AllVantage.Services.Token.Refresh.RefreshTokenService;
import com.MarketingMVP.AllVantage.Services.Token.Access.TokenService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final EmployeeDTOMapper employeeDTOMapper;
    private final ClientDTOMapper clientDTOMapper;
    private final FileService fileService;
    private final AdminDTOMapper adminDTOMapper;

    public AuthenticationServiceImpl(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder, ConfirmationTokenService confirmationTokenService, RefreshTokenService refreshTokenService, TokenService tokenService, AuthenticationManager authenticationManager, JWTService jwtService, EmployeeDTOMapper employeeDTOMapper, ClientDTOMapper clientDTOMapper, FileService fileService, AdminDTOMapper adminDTOMapper) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.employeeDTOMapper = employeeDTOMapper;
        this.clientDTOMapper = clientDTOMapper;
        this.fileService = fileService;
        this.adminDTOMapper = adminDTOMapper;
    }

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public ResponseEntity<Object> createClientAccount(ClientRegisterDTO clientRegisterDTO) {
        try {
            verifyCredentialsExistence(clientRegisterDTO.getUsername(), clientRegisterDTO.getEmail(), clientRegisterDTO.getPhoneNumber());
            Role role = roleService.fetchRoleByName("CLIENT");
            Client client = new Client();
            client.setFirstName(clientRegisterDTO.getFirstName());
            client.setLastName(clientRegisterDTO.getLastName());
            client.setUsername(clientRegisterDTO.getUsername());
            client.setEmail(clientRegisterDTO.getEmail().toLowerCase());
            client.setPhoneNumber(clientRegisterDTO.getPhoneNumber());
            client.setCreationDate(new Date());
            client.setPassword(passwordEncoder.encode(clientRegisterDTO.getPassword()));
            client.setLocked(false);
            client.setEnabled(false);
            client.setRole(role);
            client.setSuits(new ArrayList<>());
            Client saveClient = userService.saveClient(client);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(saveClient);
            String refreshToken = refreshTokenService.generateRefreshToken(saveClient);

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
            verifyCredentialsExistence(employeeRegisterDTO.getUsername(), employeeRegisterDTO.getEmail(), employeeRegisterDTO.getPhoneNumber());

            Role role = roleService.fetchRoleByName("EMPLOYEE");
            Employee employee = new Employee();
            employee.setFirstName(employeeRegisterDTO.getFirstName());
            employee.setLastName(employeeRegisterDTO.getLastName());
            employee.setUsername(employeeRegisterDTO.getUsername());
            employee.setEmail(employeeRegisterDTO.getEmail().toLowerCase());
            employee.setPhoneNumber(employeeRegisterDTO.getPhoneNumber());
            employee.setCreationDate(new Date());
            employee.setPassword(passwordEncoder.encode(employeeRegisterDTO.getPassword()));
            employee.setLocked(true);
            employee.setEnabled(false);
            employee.setSuits(new ArrayList<>());
            employee.setRole(role);

            Employee savedEmployee = userService.saveEmployee(employee);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(savedEmployee);
            String refreshToken = refreshTokenService.generateRefreshToken(savedEmployee);

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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserEntity user = userService.getUserByUsername(loginDto.getEmail());
            revokeAllUserAccessTokens(user);
            revokeAllUserRefreshToken(user);

            String jwtAccessToken = revokeGenerateAndSaveToken(user);
            String jwtRefreshToken = refreshTokenService.generateRefreshToken(user);

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(3600)
                    .sameSite("Strict")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtRefreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(604800)
                    .sameSite("Strict")
                    .build();

            Object loginResponse;

            switch (user.getRole().getName()) {
                case "CLIENT":
                    loginResponse = ClientLogInResponseDTO.builder()
                            .clientDTO(clientDTOMapper.apply(userService.getClientById(user.getId())))
                            .build();
                    break;

                case "EMPLOYEE":
                    loginResponse = EmployeeLoginResponseDTO.builder().build();
                    break;

                case "ADMIN":
                    loginResponse = AdminLoginResponseDTO.builder()
                            .adminDTO(adminDTOMapper.apply(userService.getAdminById(user.getId())))
                            .build();
                    break;

                default:
                    return ResponseEntity.status(500).body("User role not specified");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(loginResponse);

        } catch (ResourceNotFoundException e) {
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

            userService.enableAccount(confirmationToken.getUserEntity().getId());

            confirmationTokenService.setConfirmedAt(confirmationToken);
            return confirmationTokenService.getConfirmationPage();
        } catch (Exception e){
            return e.getMessage();
        }
    }

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

    private void verifyCredentialsExistence(String username, String email, String phoneNumber) throws IllegalArgumentException{
        if (userService.isUsernameRegistered(username)) {
            throw new IllegalArgumentException("Sorry, that username is already taken. Please choose a different one.");
        }
        if (userService.isEmailRegistered(email)) {
            throw new IllegalArgumentException("Sorry, that email is already taken. Please choose a different one.");
        }
        if (userService.isPhoneNumberRegistered(phoneNumber)) {
            throw new IllegalArgumentException("Sorry, that phone number is already taken. Please choose a different one.");
        }
    }
}
