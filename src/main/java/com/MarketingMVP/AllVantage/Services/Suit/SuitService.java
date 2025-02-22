package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.DTOs.Post.PostSendDTO;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

public interface SuitService {

    ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file);
    ResponseEntity<Object> updateSuitInfo(Long suitId, String name, String description);
    ResponseEntity<Object> updateSuitImage(Long suitId, MultipartFile file);
    ResponseEntity<Object> deleteSuit(Long suitId);

    ResponseEntity<Object> addEmployeeToSuit(Long suitId, UUID employeeId);
    ResponseEntity<Object> removeEmployeeFromSuit(Long suitId, UUID employeeId);

    ResponseEntity<Object> postToSuit(Long suitId, PostSendDTO postSendDTO, UserDetails employee);

    ResponseEntity<Object> addFacebookPageToSuit(Long suitId, Long accountId, String facebookPageId);
    ResponseEntity<Object> removeFacebookPageFromSuit(Long suitId, Long accountId);

    Suit findSuitById(Long suitId) throws ResourceNotFoundException;

    ResponseEntity<Object> getAllClientSuits(UUID clientId);

    RedirectView addAccountToSuit(Long suitId);

    ResponseEntity<Object> getSuitById(Long suitId);

    ResponseEntity<Object> addAccountToSuitCallback(Long suitId, String code);
}
