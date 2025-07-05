package com.MarketingMVP.AllVantage.Services.Token.Confirmation;

import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Token.ConfirmationToken.ConfirmationTokenRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConfirmationTokenServiceImpl implements  ConfirmationTokenService{

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationTokenServiceImpl(ConfirmationTokenRepository confirmationTokenRepository)
    {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    @Override
    public void saveConfirmationToken(@NonNull ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public String getConfirmationPage() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Email Confirmed</title>\n" +
                "    <style>\n" +
                "        @import url('https://fonts.googleapis.com/css2?family=Manrope:wght@200..800&display=swap');\n"+
                "        body {\n" +
                "            font-family: Manrope;\n" +
                "            background-color:#070708;\n" +
                "            margin: 0;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100vh;\n" +
                "        }\n" +
                "        .container {\n" +
                "            text-align: center;\n" +
                "            padding: 30px;\n" +
                "            background-color: white;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);\n" +
                "        }\n" +
                "        h1 {\n" +
                "            color: #333;\n" +
                "        }\n" +
                "        p {\n" +
                "            color: #666;\n" +
                "            margin-top: 10px;\n" +
                "        }\n" +
                "        .close-button {\n" +
                "            background-color: #007bff;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 10px 20px;\n" +
                "            border-radius: 5px;\n" +
                "            cursor: pointer;\n" +
                "            font-size: 16px;\n" +
                "            transition: background-color 0.3s;\n" +
                "        }\n" +
                "        .close-button:hover {\n" +
                "            background-color: #0056b3;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "    <h1>Email Confirmed</h1>\n" +
                "    <p>Your email address has been successfully confirmed. Thank you for joining us!</p>\n" +
                "    <button class=\"close-button\" onclick=\"window.close()\">Close</button>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";
    }

    @Override
    public String getAlreadyConfirmedPage() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Email Already Confirmed</title>\n" +
                "  <style>\n" +
                "    body {\n" +
                "      font-family: Arial, sans-serif;\n" +
                "      background-color: #007bff;\n" +
                "      margin: 0;\n" +
                "      display: flex;\n" +
                "      justify-content: center;\n" +
                "      align-items: center;\n" +
                "      height: 100vh;\n" +
                "    }\n" +
                "    .container {\n" +
                "      text-align: center;\n" +
                "      padding: 30px;\n" +
                "      background-color: white;\n" +
                "      border-radius: 10px;\n" +
                "      box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);\n" +
                "    }\n" +
                "    h1 {\n" +
                "      color: #333;\n" +
                "    }\n" +
                "    p {\n" +
                "      color: #666;\n" +
                "      margin-top: 10px;\n" +
                "    }\n" +
                "    .close-button {\n" +
                "      background-color: #007bff;\n" +
                "      color: white;\n" +
                "      border: none;\n" +
                "      padding: 10px 20px;\n" +
                "      border-radius: 5px;\n" +
                "      cursor: pointer;\n" +
                "      font-size: 16px;\n" +
                "      transition: background-color 0.3s;\n" +
                "    }\n" +
                "    .close-button:hover {\n" +
                "      background-color: #0056b3;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h1>Email Already Confirmed</h1>\n" +
                "    <p>Your email address has already been confirmed. Thank you for your continued support!</p>\n" +
                "    <button class=\"close-button\" onclick=\"window.close()\">Close</button>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>\n";
    }

    @Override
    public void deleteAllTokensByUserId(UUID id) {
        confirmationTokenRepository.deleteAllByUserEntityId(id);
    }

    @Override
    public ConfirmationToken fetchTokenByToken(String token) {
        return confirmationTokenRepository.fetchConfirmationTokenByToken(token).orElseThrow(
                ()-> new ResourceNotFoundException("This Token could not be found in our system.")
        );
    }

    @Override
    public void setConfirmedAt(ConfirmationToken confirmationToken) {
        confirmationToken.setConfirmationDate(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public String generateConfirmationToken(@NonNull UserEntity userEntity) {

        String token = userEntity.getId().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10),
                userEntity
        );
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }

}
