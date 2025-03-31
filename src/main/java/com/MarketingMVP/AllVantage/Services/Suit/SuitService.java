package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SuitService {

    ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file);
    ResponseEntity<Object> updateSuitInfo(Long suitId, String name, String description);
    ResponseEntity<Object> updateSuitImage(Long suitId, MultipartFile file);
    ResponseEntity<Object> deleteSuit(Long suitId);

    ResponseEntity<Object> addEmployeeToSuit(Long suitId, UUID employeeId);
    ResponseEntity<Object> removeEmployeeFromSuit(Long suitId, UUID employeeId);

    ResponseEntity<Object> postToSuit(Long suitId, String postSendDTOJson, UserDetails employee, List<MultipartFile> files);

    ResponseEntity<Object> addFacebookPageToSuit(Long suitId, Long accountId, String facebookPageId);
    ResponseEntity<Object> removeFacebookPageFromSuit(Long suitId, Long accountId);

    Suit findSuitById(Long suitId) throws ResourceNotFoundException;

    ResponseEntity<Object> getAllClientSuits(UUID clientId);

    ResponseEntity<Object> getSuitById(Long suitId);

    FacebookMedia test(Long fileId, Long accountId);

    ResponseEntity<Object> postReelToSuit(Long suitId, MultipartFile videoFile, String reelPostDTOJson);
}
