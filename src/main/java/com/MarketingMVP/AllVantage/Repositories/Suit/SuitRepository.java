package com.MarketingMVP.AllVantage.Repositories.Suit;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SuitRepository extends JpaRepository<Suit,Long> {

    @Query("SELECT s from Suit s where s.client.id = :clientId")
    List<Suit> findByClientId(@Param("clientId") UUID clientId);
}
