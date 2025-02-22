package com.MarketingMVP.AllVantage.Services.Token.Refresh;

import com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken.RefreshToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Token.RefreshToken.RefreshTokenRepository;
import com.MarketingMVP.AllVantage.Security.Utility.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String generateRefreshToken(@NonNull UserEntity userEntity) {
        Date expirationDate = new Date(System.currentTimeMillis() + SecurityConstants.REFRESH_JWT_EXPIRATION);
        String token = Jwts.builder()
                .setSubject(userEntity.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256 ,getSignInKey())
                .compact();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(token);
        refreshToken.setExpired(false);
        refreshToken.setCreationDate(new Date());
        refreshToken.setExpirationDate(expirationDate);
        refreshToken.setUserEntity(userEntity);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    public List<RefreshToken> fetchAllRefreshTokenByUserId(final UUID userId)
    {
        return refreshTokenRepository.fetchAllRefreshTokenByUserId(userId);
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        RefreshToken currentRefreshToken = fetchRefreshTokenByToken(refreshToken);

        if(currentRefreshToken.isExpired())
        {
            throw new IllegalStateException("This Refresh Token is expired, please log in again.");
        }
        if(currentRefreshToken.isRevoked())
        {
            throw new IllegalStateException("This Refresh Token is revoked.");
        }

        return true;

    }
    @Override
    public void saveAll(List<RefreshToken> refreshTokenList) {
        refreshTokenRepository.saveAll(refreshTokenList);
    }


    public RefreshToken fetchRefreshTokenByToken(final String refreshToken)
    {
        return refreshTokenRepository.fetchByToken(refreshToken).orElseThrow(
                ()-> new ResourceNotFoundException("This refresh Token could not be found in our system.")
        );
    }

    private Key getSignInKey() {
        byte [] keyBytes = Decoders.BASE64.decode(SecurityConstants.JWT_REFRESH_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
