package com.MarketingMVP.AllVantage.Security.Configuration;


import com.MarketingMVP.AllVantage.Security.JWT.CustomLogoutHandler;
import com.MarketingMVP.AllVantage.Security.JWT.JWTAuthenticationFilter;
import com.MarketingMVP.AllVantage.Security.JWT.JwtAuthEntryPoint;
import jakarta.servlet.Filter;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint authEntryPoint;
    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(JwtAuthEntryPoint authEntryPoint, CustomLogoutHandler logoutHandler) {
        this.logoutHandler = logoutHandler;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                cors(corsConfigurer -> {
                    corsConfigurer.configurationSource(corsConfigurationSource());
                }).csrf(AbstractHttpConfigurer::disable)

                .exceptionHandling(exceptionHandlingConfigurer -> {
                    exceptionHandlingConfigurer.authenticationEntryPoint(authEntryPoint);
                })

                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry
                            .requestMatchers("/api/v1/auth/login",
                                    "/api/v1/auth/confirm",
                                    "/api/v1/auth/me",
                                    "/api/v1/suit/**").permitAll()
                            .requestMatchers("/api/v1/user/add_client",
                                    "/api/v1/user/add_employee",
                                    "/api/v1/user/{id}/add_suit",
                                    "/api/v1/user/{id}/lock",
                                    "/api/v1/user/{id}/unlock",
                                    "/api/v1/user/{id}/delete").hasRole("ADMIN")
                            .requestMatchers("/api/v1/suit/{suitId}/unlink-fb",
                                    "/api/v1/suit/{suitId}/unlink-ig",
                                    "/api/v1/suit/{suitId}/unlink-li",
                                    "/api/v1/suit/{suitId}/add-fb",
                                    "/api/v1/suit/{suitId}/add-ig",
                                    "/api/v1/suit/{suitId}/add-li",
                                    "/api/v1/suit/{suitId}/add_image",
                                    "/api/v1/suit/{suitId}/delete",
                                    "/api/v1/suit/{suitId}/add-employee",
                                    "/api/v1/suit/{suitId}/remove-employee",
                                    "/api/v1/suit/{suitId}/deactivate",
                                    "/api/v1/suit/{suitId}/reactivate"
                            ).hasRole("ADMIN")
                            .requestMatchers("/api/v1/chat/**").hasAnyRole("ADMIN", "EMPLOYEE", "CLIENT")
                            .requestMatchers("/api/v1/user/add_image",
                                    "/api/v1/user/clients",
                                    "/api/v1/user/employees",
                                    "/api/v1/user/{id}").permitAll()
                            .requestMatchers("/api/v1/files/{fileId}").permitAll()

                            .anyRequest().authenticated();
                })

                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .logout(logoutConfigurer -> {
                    logoutConfigurer
                            .logoutUrl("/api/v1/auth/logout")
                            .addLogoutHandler(logoutHandler)
                            .logoutSuccessHandler((request, response, authentication) -> {
                                SecurityContextHolder.clearContext();
                                response.setStatus(200);
                            });
                })

                .httpBasic(Customizer.withDefaults());

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // must be exact
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Cookie", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(@NonNull AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    Filter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

}
