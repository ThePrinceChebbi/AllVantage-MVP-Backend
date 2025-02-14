package com.MarketingMVP.AllVantage.Services.Token;

import com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken.Token;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface TokenService {

    Token getTokenByToken(final String token);
    List<Token> fetchAllValidTokenByUserId(final UUID userId);
    Token save(@NonNull final Token token);
    List<Token> saveAll(List<Token> tokens);

}
