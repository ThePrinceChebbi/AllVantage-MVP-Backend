package com.MarketingMVP.AllVantage.DTOs.UserEntity;

import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserDTOMapper implements Function<UserEntity, UserDTO> {
    @Override
    public UserDTO apply(UserEntity userEntity) {
        return new UserDTO(
                userEntity.getId(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getPhoneNumber(),
                "http://localhost:8080/api/v1/files/" + userEntity.getImage().getId(),
                userEntity.getRole(),
                userEntity.getCreationDate(),
                userEntity.isEnabled(),
                userEntity.isLocked()
        );
    }
}
