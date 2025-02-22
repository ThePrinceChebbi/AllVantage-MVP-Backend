package com.MarketingMVP.AllVantage.Controllers.Suit;

import com.MarketingMVP.AllVantage.Services.Suit.SuitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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

    @GetMapping("/{suitId}/add-account")
    public RedirectView addAccountToSuit(@PathVariable Long suitId) {
        return suitService.addAccountToSuit(suitId);
    }
    @GetMapping("/{suitId}/callback")
    public ResponseEntity<Object> addAccountToSuitCallback(@PathVariable Long suitId, @RequestParam("code") String code) {
        return suitService.addAccountToSuitCallback(suitId, code);
    }

    @PostMapping("/{suitId}/add-employee")
    public ResponseEntity<Object> addEmployeeToSuit(@PathVariable Long suitId, @RequestParam UUID employeeId) {
        return suitService.addEmployeeToSuit(suitId, employeeId);
    }

}
