package com.MarketingMVP.AllVantage.Services.Token.Confirmation;



import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import lombok.NonNull;

import java.util.UUID;

public interface ConfirmationTokenService {

    ConfirmationToken fetchTokenByToken(final String token);
    String generateConfirmationToken(@NonNull UserEntity userEntity);
    void setConfirmedAt(final ConfirmationToken confirmationToken);
    void saveConfirmationToken(@NonNull ConfirmationToken confirmationToken);

    String getConfirmationPage();
    String getAlreadyConfirmedPage();

    void deleteAllTokensByUserId(UUID id);
}
