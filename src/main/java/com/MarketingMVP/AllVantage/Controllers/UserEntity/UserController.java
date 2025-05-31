package com.MarketingMVP.AllVantage.Controllers.UserEntity;

import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.Client.ClientDTO;
import com.MarketingMVP.AllVantage.DTOs.UserEntity.UserDTO;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Services.Authentication.AuthenticationService;
import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final AuthenticationService authenticationService;
    private final SuitService suitService;
    private final UserService userService;

    public UserController(AuthenticationService authenticationService, SuitService suitService, UserService userService) {
        this.authenticationService = authenticationService;
        this.suitService = suitService;
        this.userService = userService;
    }

    @PostMapping("/add_client")
    public ResponseEntity<Object> addClient(@RequestBody ClientRegisterDTO clientRegisterDTO) {
        return authenticationService.createClientAccount(clientRegisterDTO);
    }

    @PostMapping("/add_employee")
    public ResponseEntity<Object> addEmployee(@RequestBody EmployeeRegisterDTO employeeRegisterDTO) {
        return authenticationService.createEmployeeAccount(employeeRegisterDTO);
    }

    @PostMapping("/{id}/add_suit")
    public ResponseEntity<Object> addSuit(@RequestParam("name") String name, @RequestParam("description") String description, @RequestParam("suitColor") String suitColor, @PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return suitService.addNewSuit(name, description, id, file, suitColor);
    }

    @PostMapping("/{id}/add_image")
    public ResponseEntity<Object> addImage(@RequestParam("file") MultipartFile file, @PathVariable UUID id) {
        return userService.addImage(file, id);
    }

    @GetMapping("/")
    public ResponseEntity<Object> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/clients")
    public ResponseEntity<Object> getAllClients(@RequestParam("pageNumber") int pageNumber) {
        return userService.getAllClients(pageNumber);
    }

    @GetMapping("/employees")
    public ResponseEntity<Object> getAllEmployees(@RequestParam("pageNumber") int pageNumber) {
        return userService.getAllEmployees(pageNumber);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccountById(@PathVariable UUID id) {
        return userService.getAccountById(id);
    }

    @PutMapping("/{id}/info")
    public ResponseEntity<Object> updateUserInfo(@PathVariable UUID id, @RequestBody UserEntity userEntity, @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateUserInfo(id, userEntity, userDetails);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<Object> lockAccount(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        return userService.lockAccount(id, userDetails);
    }
    @PutMapping("/{id}/unlock")
    public ResponseEntity<Object> unlockAccount(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        return userService.unlockAccount(id, userDetails);
    }

}
