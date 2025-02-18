package com.MarketingMVP.AllVantage.DTOs.Authentication.Admin;

import com.MarketingMVP.AllVantage.DTOs.UserEntity.Admin.AdminDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRegisterResponseDTO {
    private AdminDTO adminDTO;
    private String confirmationToken;
    private String refreshToken;

    public AdminRegisterResponseDTO(AdminDTO adminDTO, String confirmationToken, String refreshToken) {
        this.adminDTO = adminDTO;
        this.confirmationToken = confirmationToken;
        this.refreshToken = refreshToken;
    }
}
