package com.MarketingMVP.AllVantage.DTOs.UserEntity;

import com.MarketingMVP.AllVantage.Entities.Role.Role;
import java.util.Date;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        String country,
        String state,
        String address,
        String postalCode,
        String imageUrl,
        Role role,
        Date creationDate,
        boolean isEnabled,
        boolean isLocked
) {
}
