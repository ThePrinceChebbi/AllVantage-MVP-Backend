package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Suit.SuitDTO;
import com.MarketingMVP.AllVantage.Entities.Account.Facebook.Page.FacebookPage;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SuitService {

    ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file);

    SuitDTO addFacebookPageToSuit(Long suitId, FacebookPage facebookPage);
    SuitDTO removeFacebookPageFromSuit(Long suitId, Long accountId);

    Suit findSuitById(Long suitId) throws ResourceNotFoundException;

}
