package com.MarketingMVP.AllVantage.Controllers.Suit;

import com.MarketingMVP.AllVantage.Entities.PlatformContent.Facebook.FacebookMedia;
import com.MarketingMVP.AllVantage.Services.Accounts.Facebook.FacebookService;
import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suit")
public class SuitController {

    private final SuitService suitService;
    private final FacebookService facebookService;

    public SuitController(SuitService suitService, FacebookService facebookService) {
        this.suitService = suitService;
        this.facebookService = facebookService;
    }

    @PostMapping("/{accountId}/{suitId}/add-page")
    public ResponseEntity<Object> addFacebookPageToSuit(
            @PathVariable Long accountId,
            @PathVariable Long suitId,
            @RequestParam String pageId)
    {
        return suitService.addFacebookPageToSuit(suitId, accountId, pageId);
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

    @GetMapping("/test")
    public FacebookMedia test(@RequestParam Long fileId, @RequestParam Long accountId) {
        return suitService.test(fileId, accountId);
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

    @PostMapping("/{suitId}/post-facebook")
    public ResponseEntity<Object> postToSuit(
            @PathVariable Long suitId,
            @RequestParam List<MultipartFile> files,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Date scheduledAt,
            @RequestParam Long pageId
            )
    {
        return suitService.postToFacebook(suitId, files, title, content, scheduledAt, pageId);
    }

    @PostMapping("/{suitId}/test-post")
    public String testPost(@PathVariable Long suitId, @RequestParam List<String> mediaIds){
        return facebookService.testPostingWithMediaIds(mediaIds,suitId);
    }

    @PostMapping("/test_video_init")
    public String testVideo(@RequestParam Long pageId){
        return facebookService.initVideo(pageId);
    }

    @PostMapping("/test_video_upload")
    public ResponseEntity<String> testVideoUpload(@RequestParam Long pageId, @RequestParam MultipartFile video, @RequestParam String videoId){
        return facebookService.uploadVideoToFacebook(pageId, video, videoId);
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
}
