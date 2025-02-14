package com.MarketingMVP.AllVantage.DTOs.UserEntity;




import com.MarketingMVP.AllVantage.Entities.Role.Role;

import java.util.Date;
import java.util.UUID;

public record ClientDTO(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        Role role,
        Date creationDate,
        boolean isEnabled,
        boolean isLocked
) {
}
