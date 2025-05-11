package com.MarketingMVP.AllVantage.Controllers.UserEntity;

import com.MarketingMVP.AllVantage.DTOs.Authentication.Client.ClientRegisterDTO;
import com.MarketingMVP.AllVantage.DTOs.Authentication.Employee.EmployeeRegisterDTO;
import com.MarketingMVP.AllVantage.Services.Authentication.AuthenticationService;
import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import com.MarketingMVP.AllVantage.Services.UserEntity.UserService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> addSuit(@RequestParam("name") String name, @RequestParam("description") String description, @PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return suitService.addNewSuit(name, description, id, file);
    }

    @PostMapping("/{id}/add_image")
    public ResponseEntity<Object> addImage(@RequestParam("file") MultipartFile file, @PathVariable UUID id) {
        return userService.addImage(file, id);
    }

    @GetMapping("/")
    public ResponseEntity<Object> getAll() {
        return userService.getAllUsers();
    }

}
