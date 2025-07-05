package com.MarketingMVP.AllVantage.Services.Suit;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface SuitService {

    ResponseEntity<Object> addNewSuit(String name, String description, UUID clientId, MultipartFile file, String suitColor);
    ResponseEntity<Object> updateSuitInfo(Long suitId, String suitColor, String name, String description, MultipartFile file, UserDetails userDetails);
    ResponseEntity<Object> deactivateSuit(Long suitId, UserDetails userDetails);
    ResponseEntity<Object> reactivateSuit(Long suitId, UserDetails userDetails);

    ResponseEntity<Object> addEmployeeToSuit(Long suitId, UUID employeeId);
    ResponseEntity<Object> removeEmployeeFromSuit(Long suitId, UUID employeeId);

    ResponseEntity<Object> addFacebookPageToSuit(Long suitId, Long accountId, String facebookPageId);

    ResponseEntity<Object> addInstagramAccountToSuit(Long suitId, Long pageId, String instagramAccountId);

    ResponseEntity<Object> addLinkedInOrganizationToSuit(Long suitId, Long accountId, String linkedInOrganizationId);

    ResponseEntity<Object> removeFacebookPageFromSuit(Long suitId, Long pageID);

    ResponseEntity<Object> removeInstagramAccountFromSuit(Long suitId, Long accountId);

    ResponseEntity<Object> removeLinkedInOrgFromSuit(Long suitId, Long orgId);

    Suit findSuitById(Long suitId) throws ResourceNotFoundException;

    ResponseEntity<Object> getAllClientSuits(UUID clientId);

    ResponseEntity<Object> getSuitById(Long suitId, UserDetails userDetails);

    ResponseEntity<Object> getAllSuitPosts(Long suitId, int pageNumber, UserDetails userDetails);

    ResponseEntity<Object> postToSuit(Long suitId, String postSendDTOJson, List<MultipartFile> files, UserDetails userDetails);

    ResponseEntity<Object> postReelToSuit(Long suitId, MultipartFile videoFile, String reelPostDTOJson, UserDetails userDetails);

    ResponseEntity<Object> createStory(Long suitId, MultipartFile videoFile, String reelPostDTOJson, UserDetails userDetails);

    ResponseEntity<Object> getAllSuits(UserDetails userDetails);

    ResponseEntity<Object> getPostInsights(Long suitId, Long postId);

    ResponseEntity<Object> getUsersBySuitId(Long suitId);

    ResponseEntity<Object> getPostById(Long suitId, Long postId);

    ResponseEntity<Object> getPostingFrequency(Long suitId);

    ResponseEntity<Object> getAllPostingFrequencies(UserDetails userDetails);

    ResponseEntity<Object> deletePostFromSuit(Long suitId, Long postId, UserDetails userDetails);
}
