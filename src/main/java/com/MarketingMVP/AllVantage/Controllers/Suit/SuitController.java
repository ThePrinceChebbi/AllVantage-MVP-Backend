package com.MarketingMVP.AllVantage.Controllers.Suit;

import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import jakarta.annotation.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suit")
public class SuitController {

    private final SuitService suitService;

    public SuitController(SuitService suitService) {
        this.suitService = suitService;
    }

    @PostMapping("/{accountId}/{suitId}/add-fb")
    public ResponseEntity<Object> addFacebookPageToSuit(
            @PathVariable Long accountId,
            @PathVariable Long suitId,
            @RequestParam String pageId)
    {
        return suitService.addFacebookPageToSuit(suitId, accountId, pageId);
    }

    @PostMapping("/{pageId}/{suitId}/add-ig")
    public ResponseEntity<Object> addInstagramAccountToSuit(
            @PathVariable Long pageId,
            @PathVariable Long suitId,
            @RequestParam String igId)
    {
        return suitService.addInstagramAccountToSuit(suitId, pageId, igId);
    }

    @PostMapping("/{accountId}/{suitId}/add-li")
    public ResponseEntity<Object> addLinkedInOrganizationToSuit(
            @PathVariable Long accountId,
            @PathVariable Long suitId,
            @RequestParam String orgId)
    {
        return suitService.addLinkedInOrganizationToSuit(suitId, accountId, orgId);
    }

    @PutMapping("/{suitId}/unlink-fb")
    public ResponseEntity<Object> removeFacebookPageFromSuit(
            @PathVariable Long suitId,
            @RequestParam Long pageId)
    {
        return suitService.removeFacebookPageFromSuit(suitId, pageId);
    }

    @PutMapping("/{suitId}/unlink-ig")
    public ResponseEntity<Object> removeInstagramAccountFromSuit(
            @PathVariable Long suitId,
            @RequestParam Long igId)
    {
        return suitService.removeInstagramAccountFromSuit(suitId, igId);
    }

    @PutMapping("/{suitId}/unlink-li")
    public ResponseEntity<Object> removeLinkedInOrganizationFromSuit(
            @PathVariable Long suitId,
            @RequestParam Long orgId)
    {
        return suitService.removeLinkedInOrgFromSuit(suitId, orgId);
    }

    @GetMapping("/{clientId}/all")
    public ResponseEntity<Object> getAllClientSuits(@PathVariable UUID clientId) {
        return suitService.getAllClientSuits(clientId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllSuits(@AuthenticationPrincipal UserDetails userDetails) {
        return suitService.getAllSuits(userDetails);
    }

    @PostMapping("/{suitId}/add-employee")
    public ResponseEntity<Object> addEmployeeToSuit(@PathVariable Long suitId, @RequestParam UUID employeeId) {
        return suitService.addEmployeeToSuit(suitId, employeeId);
    }

    @PostMapping("/{suitId}/remove-employee")
    public ResponseEntity<Object> removeEmployeeFromSuit(@PathVariable Long suitId, @RequestParam UUID employeeId) {
        return suitService.removeEmployeeFromSuit(suitId, employeeId);
    }

    @GetMapping("/{suitId}")
    public ResponseEntity<Object> getSuitById(@PathVariable Long suitId, @AuthenticationPrincipal UserDetails userDetails) {
        return suitService.getSuitById(suitId, userDetails);
    }

    @GetMapping("/{suitId}/posts")
    public ResponseEntity<Object> getAllSuitPosts(@PathVariable Long suitId, @RequestParam int pageNumber, @AuthenticationPrincipal UserDetails userDetails) {
        return suitService.getAllSuitPosts(suitId, pageNumber,userDetails);
    }

    @PostMapping("/{suitId}/post")
    public ResponseEntity<Object> postToSuit(
            @PathVariable Long suitId,
            @RequestParam String postSendDTOJson,
            @AuthenticationPrincipal UserDetails employee,
            @RequestParam List<MultipartFile> files
    ) {
        System.out.println(postSendDTOJson);
        return suitService.postToSuit(suitId, postSendDTOJson, files, employee);
    }

    @PutMapping("/{suitId}/update")
    public ResponseEntity<Object> updateSuitInfo(
            @PathVariable Long suitId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("suitColor") String suitColor,
            @Nullable @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return suitService.updateSuitInfo(suitId, suitColor, name, description, file, userDetails);
    }

    @PostMapping("/{suitId}/reel")
    public ResponseEntity<Object> postReelToSuit(
            @PathVariable Long suitId,
            @RequestParam String reelPostDTOJson,
            @AuthenticationPrincipal UserDetails employee,
            @RequestParam MultipartFile videoFile
    )
    {
        return suitService.postReelToSuit(suitId, videoFile, reelPostDTOJson, employee);
    }

    @PostMapping("/{suitId}/story")
    public ResponseEntity<Object> postStoryToSuit(
            @PathVariable Long suitId,
            @RequestParam String storySendJson,
            @AuthenticationPrincipal UserDetails employee,
            @RequestParam MultipartFile videoFile
    )
    {
        return suitService.createStory(suitId, videoFile, storySendJson, employee);
    }

    @GetMapping("/{suitId}/post-insights")
    public ResponseEntity<Object> getPostInsights(@PathVariable Long suitId, @RequestParam Long postId) {
        return suitService.getPostInsights(suitId, postId);
    }

    @GetMapping("/{suitId}/{postId}")
    public ResponseEntity<Object> getPostById(@PathVariable Long suitId, @PathVariable Long postId) {
        return suitService.getPostById(suitId,postId);
    }

    @GetMapping("/{suitId}/users")
    public ResponseEntity<Object> getUsers(@PathVariable Long suitId) {
        return suitService.getUsersBySuitId(suitId);
    }

    @GetMapping("/{suitId}/posting-frequency")
    public ResponseEntity<Object> getPostingFrequency(@PathVariable Long suitId) {
        return suitService.getPostingFrequency(suitId);
    }

    @GetMapping("posting-frequency")
    public ResponseEntity<Object> getAllPostingFrequencies(@AuthenticationPrincipal UserDetails userDetails) {
        return suitService.getAllPostingFrequencies(userDetails);
    }

    @DeleteMapping("/{suitId}")
    public ResponseEntity<Object> deactivateSuit(@PathVariable Long suitId, @AuthenticationPrincipal UserDetails userDetails) {
        return suitService.deactivateSuit(suitId, userDetails);
    }

    @PutMapping("/{suitId}/reactivate")
    public ResponseEntity<Object> reactivateSuit(@PathVariable Long suitId, @AuthenticationPrincipal UserDetails userDetails) {
        return suitService.reactivateSuit(suitId, userDetails);
    }

    @DeleteMapping("/{suitId}/{postId}")
    public ResponseEntity<Object> deletePostFromSuit(@PathVariable Long suitId, @PathVariable Long postId, @AuthenticationPrincipal UserDetails userDetails) {
        return suitService.deletePostFromSuit(suitId, postId, userDetails);
    }
}
