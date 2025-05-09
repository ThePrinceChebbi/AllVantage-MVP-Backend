package com.MarketingMVP.AllVantage.Controllers.Suit;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.Meta.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/{accountId}/{suitId}/add-ig")
    public ResponseEntity<Object> addInstagramAccountToSuit(
            @PathVariable Long accountId,
            @PathVariable Long suitId,
            @RequestParam String igId)
    {
        return suitService.addInstagramAccountToSuit(suitId, accountId, igId);
    }

    @PostMapping("/{accountId}/{suitId}/add-li")
    public ResponseEntity<Object> addLinkedInOrganizationToSuit(
            @PathVariable Long accountId,
            @PathVariable Long suitId,
            @RequestParam String orgId)
    {
        return suitService.addLinkedInOrganizationToSuit(suitId, accountId, orgId);
    }

    @GetMapping("/{clientId}/all")
    public ResponseEntity<Object> getAllSuits(@PathVariable UUID clientId) {
        return suitService.getAllClientSuits(clientId);
    }

    @PostMapping("/{suitId}/add-employee")
    public ResponseEntity<Object> addEmployeeToSuit(@PathVariable Long suitId, @RequestParam UUID employeeId) {
        return suitService.addEmployeeToSuit(suitId, employeeId);
    }

    @GetMapping("/{suitId}")
    public ResponseEntity<Object> getSuitById(@PathVariable Long suitId) {
        return suitService.getSuitById(suitId);
    }

    @GetMapping("/{suitId}/posts")
    public ResponseEntity<Object> getAllSuitPosts(@PathVariable Long suitId, @RequestParam int pageNumber) {
        return suitService.getAllSuitPosts(suitId, pageNumber);
    }

    @PostMapping("/{suitId}/post")
    public ResponseEntity<Object> postToSuit(
            @PathVariable Long suitId,
            @RequestParam String postSendDTOJson,
            //@AuthenticationPrincipal UserDetails employee,
            @RequestParam List<MultipartFile> files
    ) {
        return suitService.postToSuit(suitId, postSendDTOJson, null, files);
    }

    @PostMapping("/{suitId}/reel")
    public ResponseEntity<Object> postReelToSuit(
            @PathVariable Long suitId,
            @RequestParam String reelPostDTOJson,
            //@AuthenticationPrincipal UserDetails employee,
            @RequestParam MultipartFile videoFile
    )
    {
        return suitService.postReelToSuit(suitId, videoFile, reelPostDTOJson);
    }

    @GetMapping("/{suitId}/post-insights")
    public ResponseEntity<Object> getPostInsights(@PathVariable Long suitId, @RequestParam Long postId) {
        return suitService.getPostInsights(suitId, postId);
    }
}
