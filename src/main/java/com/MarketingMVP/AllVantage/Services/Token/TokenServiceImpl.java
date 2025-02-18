package com.MarketingMVP.AllVantage.Services.Token;

import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.Token;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Token.AccessToken.TokenRepository;
import com.MarketingMVP.AllVantage.Security.JWT.JWTService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService{
    private final TokenRepository tokenRepository;
    private final JWTService jwtService;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository, JWTService jwtService)
    {
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }


    @Override
    public Token getTokenByToken(String token) {
        return tokenRepository.findByToken(token).orElseThrow(
                ()-> new ResourceNotFoundException("The token u provided could not be found in our system")
        );
    }

    @Override
    public List<Token> fetchAllValidTokenByUserId(UUID userId) {
        return tokenRepository.fetchAllValidTokenByUserId(userId);
    }

    @Override
    public Token save(@NonNull Token token) {
        return tokenRepository.save(token);
    }

    @Override
    public List<Token> saveAll(List<Token> tokens) {
        return tokenRepository.saveAll(tokens);
    }
}
