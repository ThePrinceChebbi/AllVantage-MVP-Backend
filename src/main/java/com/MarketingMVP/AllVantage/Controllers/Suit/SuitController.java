package com.MarketingMVP.AllVantage.Controllers.Suit;

import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suit")
public class SuitController {

    private final SuitService suitService;

    public SuitController(SuitService suitService) {
        this.suitService = suitService;
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
    public String test(@RequestParam Long fileId, @RequestParam Long accountId) {
        return suitService.test(fileId, accountId);
    }

    @PostMapping("/{suitId}/post")
    public ResponseEntity<Object> postToSuit(
            @PathVariable Long suitId,
            @RequestParam List<MultipartFile> files,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Date scheduledAt,
            @RequestParam Long pageId
            ) {
        return suitService.postToFacebook(suitId, files, title, content, scheduledAt, pageId);
    }
}
