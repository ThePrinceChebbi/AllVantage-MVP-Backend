package com.MarketingMVP.AllVantage.DTOs.UserEntity.Admin;

import com.MarketingMVP.AllVantage.Entities.UserEntity.Admin;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AdminDTOMapper implements Function<Admin, AdminDTO> {
    @Override
    public AdminDTO apply(Admin admin) {
        return new AdminDTO(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getUsername(),
                admin.getEmail(),
                "http://localhost:8080/api/v1/files/" + admin.getImage().getId(),
                admin.getPhoneNumber(),
                admin.getRole(),
                admin.getCreationDate(),
                admin.isEnabled(),
                admin.isLocked()
        );
    }
}
