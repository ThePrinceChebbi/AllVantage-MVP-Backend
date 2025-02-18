package com.MarketingMVP.AllVantage.Repositories.Token.ConfirmationToken;


import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {


    @Query(value =  "SELECT CT FROM ConfirmationToken CT WHERE  CT.confirmationToken = :token")
    Optional<ConfirmationToken> fetchConfirmationTokenByToken(@Param("token") final String token);
}
