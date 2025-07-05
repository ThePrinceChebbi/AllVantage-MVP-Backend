package com.MarketingMVP.AllVantage.Repositories.Chat;

import com.MarketingMVP.AllVantage.Entities.Chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE c.id = :id AND c.user.id = :userId")
    Optional<Chat> findByIdAndUser(@Param("id") Long id, @Param("userId") UUID userId);

    @Query("SELECT c FROM Chat c WHERE c.user.id = :userId")
    List<Chat> findAllByUser(UUID userId);
}
