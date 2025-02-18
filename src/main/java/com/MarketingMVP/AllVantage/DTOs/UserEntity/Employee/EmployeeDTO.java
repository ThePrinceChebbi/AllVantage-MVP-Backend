package com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee;

import com.MarketingMVP.AllVantage.Entities.Role.Role;

import java.util.Date;
import java.util.UUID;

public record EmployeeDTO(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        Role role,
        String phoneNumber,
        Date creationDate,
        boolean isEnabled,
        boolean isLocked
) {
}
