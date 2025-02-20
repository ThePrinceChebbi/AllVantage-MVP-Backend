package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SuitService {

    ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file);


    Suit saveSuit(Suit suit);
    Suit findSuitById(Long suitId) throws ResourceNotFoundException;
    void deleteSuit(Long suitId);

}
