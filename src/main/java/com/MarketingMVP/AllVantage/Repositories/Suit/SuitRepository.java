package com.MarketingMVP.AllVantage.Repositories.Suit;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SuitRepository extends JpaRepository<Suit,Long> {

    @Query("SELECT s from Suit s where s.client.id = :clientId and s.isActive=true")
    List<Suit> findByClientId(@Param("clientId") UUID clientId);

    @Query("SELECT count(p) FROM Suit s JOIN s.posts p WHERE s.id = :suitId AND TYPE(p) = Post AND CAST(p.createdAt AS date) = CAST(:date AS date) ")
    int getPostCountPerDay(@Param("suitId") Long suitId, @Param("date") Date date);

    @Query("SELECT count(p) FROM Suit s JOIN s.posts p WHERE s.id = :suitId AND TYPE(p) = Reel AND CAST(p.createdAt AS date) = CAST(:date AS date)")
    int getReelCountPerDay(@Param("suitId") Long suitId, @Param("date") Date date);

}
