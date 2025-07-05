package com.MarketingMVP.AllVantage.Security.JWT;

import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ExpiredTokenException;
import com.MarketingMVP.AllVantage.Exceptions.InvalidTokenException;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Exceptions.RevokedTokenException;
import com.MarketingMVP.AllVantage.Repositories.Token.AccessToken.TokenRepository;
import com.MarketingMVP.AllVantage.Services.Authentication.AuthenticationService;
import com.MarketingMVP.AllVantage.Services.UserDetails.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {

            String jwtToken = jwtService.extractFromCookie(request, "accessToken");

            if (jwtToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.validateToken(jwtToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsernameFromJwt(jwtToken);

            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            UserEntity userEntity = (UserEntity) this.customUserDetailsService.loadUserByUsername(username);

            var isTokenValid = tokenRepository.findByToken(jwtToken).map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
            var tokenSaved = tokenRepository.findByToken(jwtToken).orElse(null);
            if (!isTokenValid) {
                throw new ResourceNotFoundException("Token not found.");
            }

            if (jwtService.isTokenExpired(jwtToken)) {
                throw new ExpiredTokenException("Token has expired.");
            }
            if (tokenSaved.isRevoked()) {
                throw new RevokedTokenException("Token has been revoked");
            }
            if (!jwtService.isTokenValid(jwtToken, userEntity)) {

                throw new InvalidTokenException("Invalid token");
            }

            if (!jwtService.isTokenValid(jwtToken, userEntity) || !isTokenValid || !userEntity.isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEntity, null, userEntity.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        }
        catch (ExpiredTokenException ex) {
            ResponseEntity<Object> refreshResponse = authenticationService.refresh(request, response);

            if (refreshResponse.getStatusCode() != HttpStatus.OK) {
                jwtService.clearCookies(response);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired. Please log in again.");
                return;
            }

            filterChain.doFilter(request, response);
        }
        catch (Exception e)
        {
            jwtService.clearCookies(response);
            response.setHeader("error",e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, String> error = new HashMap<>();
            error.put("time_stamp" , String.valueOf(LocalDateTime.now()));
            error.put("status" , String.valueOf(HttpServletResponse.SC_FORBIDDEN));
            error.put("error_message", e.getMessage());
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(),error);
        }
    }
}
