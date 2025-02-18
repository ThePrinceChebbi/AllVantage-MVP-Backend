package com.MarketingMVP.AllVantage.Repositories.Token.RefreshToken;



import com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


    @Query(value = "SELECT R FROM RefreshToken R WHERE R.refreshToken = :refreshToken")
    Optional<RefreshToken> fetchByToken(@Param("refreshToken")final String refreshToken);

    @Query(value = "SELECT R FROM RefreshToken R WHERE R.userEntity.id = :userId")
    List<RefreshToken> fetchAllRefreshTokenByUserId(@Param("userId")final UUID userId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM RefreshToken R WHERE R.id = :refreshTokenId")
    void deleteRefreshTokenById(@Param("refreshTokenId")final long id);
}
