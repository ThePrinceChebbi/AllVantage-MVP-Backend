package com.MarketingMVP.AllVantage.DTOs.Authentication.Admin;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Admin.AdminDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponseDTO {
    private AdminDTO adminDTO;
    private String accessToken;
    private String refreshToken;
}
