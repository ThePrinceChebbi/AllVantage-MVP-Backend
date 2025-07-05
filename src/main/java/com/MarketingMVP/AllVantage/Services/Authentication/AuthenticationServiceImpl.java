package com.MarketingMVP.AllVantage.Services.Authentication;


import com.MarketingMVP.AllVantage.DTOs.Authentication.*;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterResponseDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTOMapper;
import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.Token;
import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.TokenType;
import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken.RefreshToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Exceptions.RevokedTokenException;
import com.MarketingMVP.AllVantage.Security.JWT.JWTService;
import com.MarketingMVP.AllVantage.Security.Utility.SecurityConstants;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.Mail.SimpleMailSender;
import com.MarketingMVP.AllVantage.Services.Role.RoleService;
import com.MarketingMVP.AllVantage.Services.Token.Confirmation.ConfirmationTokenService;
import com.MarketingMVP.AllVantage.Services.Token.Refresh.RefreshTokenService;
import com.MarketingMVP.AllVantage.Services.Token.Access.TokenService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

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
    private final UserDTOMapper userDTOMapper;
    private final SimpleMailSender simpleMailSender;

    public AuthenticationServiceImpl(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder, ConfirmationTokenService confirmationTokenService, RefreshTokenService refreshTokenService, TokenService tokenService, AuthenticationManager authenticationManager, JWTService jwtService, UserDTOMapper userDTOMapper, SimpleMailSender simpleMailSender) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDTOMapper = userDTOMapper;
        this.simpleMailSender = simpleMailSender;
    }

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public ResponseEntity<Object> createClientAccount(ClientRegisterDTO clientRegisterDTO) {
        try {
            verifyCredentialsExistence(clientRegisterDTO.getUsername(), clientRegisterDTO.getEmail(), clientRegisterDTO.getPhoneNumber());

            String password = generateRandomPassword(12);

            Role role = roleService.fetchRoleByName("CLIENT");
            Client client = new Client();
            client.setFirstName(clientRegisterDTO.getFirstName());
            client.setLastName(clientRegisterDTO.getLastName());
            client.setUsername(clientRegisterDTO.getUsername());
            client.setEmail(clientRegisterDTO.getEmail().toLowerCase());
            client.setPhoneNumber(clientRegisterDTO.getPhoneNumber());
            client.setCreationDate(new Date());
            client.setPassword(passwordEncoder.encode(password));
            client.setPostalCode(clientRegisterDTO.getPostalCode());
            client.setAddress(clientRegisterDTO.getAddress());
            client.setCountry(clientRegisterDTO.getCountry());
            client.setState(clientRegisterDTO.getState());
            client.setLocked(false);
            client.setEnabled(false);
            client.setRole(role);
            client.setSuits(new ArrayList<>());
            Client saveClient = userService.saveClient(client);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(saveClient);

            String confirmationLink = baseUrl + "/api/v1/auth/confirm?token=" + confirmationToken;
            simpleMailSender.sendAuthMailToClient(saveClient, password, confirmationLink);

            final ClientRegisterResponseDTO registerResponse = ClientRegisterResponseDTO
                    .builder()
                    .confirmationToken(confirmationToken)
                    .userDTO(userDTOMapper.apply(saveClient))
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

            String password = generateRandomPassword(12);

            Role role = roleService.fetchRoleByName("EMPLOYEE");
            Employee employee = new Employee();
            employee.setFirstName(employeeRegisterDTO.getFirstName());
            employee.setLastName(employeeRegisterDTO.getLastName());
            employee.setUsername(employeeRegisterDTO.getUsername());
            employee.setEmail(employeeRegisterDTO.getEmail().toLowerCase());
            employee.setPhoneNumber(employeeRegisterDTO.getPhoneNumber());
            employee.setCreationDate(new Date());
            employee.setPassword(passwordEncoder.encode(password));
            employee.setPostalCode(employeeRegisterDTO.getPostalCode());
            employee.setAddress(employeeRegisterDTO.getAddress());
            employee.setCountry(employeeRegisterDTO.getCountry());
            employee.setState(employeeRegisterDTO.getState());
            employee.setLocked(false);
            employee.setEnabled(false);
            employee.setSuits(new ArrayList<>());
            employee.setRole(role);

            Employee savedEmployee = userService.saveEmployee(employee);

            String confirmationToken = confirmationTokenService.generateConfirmationToken(savedEmployee);

            String confirmationLink = baseUrl + "/api/v1/auth/confirm?token=" + confirmationToken;
            simpleMailSender.sendAuthMailToEmployee(savedEmployee, password, confirmationLink);

            final EmployeeRegisterResponseDTO registerResponse = EmployeeRegisterResponseDTO
                    .builder()
                    .confirmationToken(confirmationToken)
                    .userDTO(userDTOMapper.apply(savedEmployee))
                    .build();

            return ResponseEntity.status(200).body(registerResponse);
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }


    @Override
    public ResponseEntity<Object> login(@NonNull LoginDTO loginDto) {
        try {
            if (loginDto.getUsername() == null || loginDto.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password must not be empty");
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()
                    )
            );

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserEntity user = userService.getUserByUsername(loginDto.getUsername());
            revokeAllUserRefreshToken(user);

            String jwtAccessToken = revokeGenerateAndSaveToken(user);

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(SecurityConstants.ACCESS_JWT_EXPIRATION)
                    .sameSite("Strict")
                    .build();

            if (loginDto.isRememberMe()) {
                String jwtRefreshToken = refreshTokenService.generateRefreshToken(user);

                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtRefreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(SecurityConstants.REFRESH_JWT_EXPIRATION)
                        .sameSite("Strict")
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                        .body(userDTOMapper.apply(userService.getUserById(user.getId())));
            }else{
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .body(userDTOMapper.apply(userService.getUserById(user.getId())));
            }

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
    public ResponseEntity<Object> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. Extract tokens from cookies
            String refreshToken = getCookieValue(request, "refreshToken");
            String expiredAccessToken = getCookieValue(request, "accessToken");

            // 2. Validate tokens and fetch data
            final Token currentToken = tokenService.getTokenByToken(expiredAccessToken);
            final UserEntity currentUser = currentToken.getUserEntity();
            final RefreshToken currentRefreshToken = refreshTokenService.fetchRefreshTokenByToken(refreshToken);
            final boolean isRefreshTokenValid = refreshTokenService.validateRefreshToken(refreshToken);

            if (!isRefreshTokenValid) {
                throw new RevokedTokenException("Refresh token is revoked or expired.");
            }

            if (currentRefreshToken.getUserEntity().getId() != currentUser.getId()) {
                throw new IllegalStateException("The access token and refresh token u provided are not compatible.");
            }

            String jwtAccessToken = revokeGenerateAndSaveToken(currentUser);
            String newRefreshToken = refreshTokenService.generateRefreshToken(currentUser);

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtAccessToken)
                    .httpOnly(true).secure(true).sameSite("Strict")
                    .path("/").maxAge(3600).build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true).secure(true).sameSite("Strict")
                    .path("/").maxAge( 7 * 24 * 3600).build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(Map.of("message", "Tokens refreshed"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        return Arrays.stream(Optional.ofNullable(request.getCookies())
                        .orElseThrow(() -> new ResourceNotFoundException("No cookies found")))
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(name + " cookie not found"));
    }

    @Override
    public ResponseEntity<UserDTO> getMe(UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        UserDTO userDTO = new UserDTOMapper().apply(user);

        return ResponseEntity.ok(userDTO);
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
