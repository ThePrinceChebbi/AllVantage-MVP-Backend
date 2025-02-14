package com.MarketingMVP.AllVantage.Services.Token;

import com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken.RefreshToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {

    String generateRefreshToken(@NonNull final UserEntity userEntity);
    List<RefreshToken> fetchAllRefreshTokenByUserId(final UUID userId);
    RefreshToken fetchRefreshTokenByToken(final String refreshToken);
    boolean validateRefreshToken(final String refreshToken);
    void saveAll(List<RefreshToken> refreshTokenList);
}
