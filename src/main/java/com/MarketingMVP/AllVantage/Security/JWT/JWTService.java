package com.MarketingMVP.AllVantage.Security.JWT;



import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ExpiredTokenException;
import com.MarketingMVP.AllVantage.Exceptions.InvalidTokenException;
import com.MarketingMVP.AllVantage.Security.Utility.SecurityConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;


@Component
public class JWTService {

    public String generateToken(@NonNull UserEntity userEntity)
    {
        String username = userEntity.getUsername();
        Date currentData = new Date();
        Date expireDate = new Date(System.currentTimeMillis() + SecurityConstants.ACCESS_JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentData)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256,getSignInKey())
                .compact();
    }
    public boolean validateToken(String token) {
        try{
            Claims claims = extractAllClaims(token);
        }
        catch (ExpiredJwtException ex)
        {
            throw new ExpiredJwtException(null,null,"Token has expired. Please log in again.", ex);
        }
        return true;
    }

    public boolean isTokenExpired(String token)
    {
        return extractExpirationDate(token).before(new Date());
    }
    public boolean isTokenValid(String token, @NotNull UserEntity userEntity)
    {
        final String username = extractUsernameFromJwt(token);
        return username.equals(userEntity.getUsername()) && !isTokenExpired(token);
    }
    public <T> T extractClaim(String token , @NotNull Function<Claims,T> claimResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Token has expired.");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while parsing token", e);
        }
    }

    public Date extractExpirationDate(String token)
    {
        return extractClaim(token,Claims::getExpiration);
    }

    public Date extractIssuedAtDate(String token)
    {
        return extractClaim(token , Claims::getIssuedAt);
    }
    public String extractUsernameFromJwt(String token)
    {
        return extractClaim(token , Claims::getSubject);
    }


    private Key getSignInKey() {
        byte [] keyBytes = Decoders.BASE64.decode(SecurityConstants.JWT_ACCESS_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) return cookie.getValue();
        }
        return null;
    }

    public void clearCookies(HttpServletResponse response) {
        Cookie access = new Cookie("accessToken", null);
        access.setMaxAge(0);
        access.setPath("/");
        response.addCookie(access);

        Cookie refresh = new Cookie("refreshToken", null);
        refresh.setMaxAge(0);
        refresh.setPath("/");
        response.addCookie(refresh);
    }


}
