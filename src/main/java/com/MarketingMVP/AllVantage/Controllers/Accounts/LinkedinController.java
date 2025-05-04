package com.MarketingMVP.AllVantage.Controllers.Accounts;


import com.MarketingMVP.AllVantage.DTOs.Response.Postable.PlatformPostResult;
import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import com.MarketingMVP.AllVantage.Services.Platform_Specific.LinkedIn.LinkedInService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account/linkedin")
public class LinkedinController {

    private final LinkedInService linkedInService;
    private final FileService fileService;

    public LinkedinController(LinkedInService linkedInService, FileService fileService) {
        this.linkedInService = linkedInService;
        this.fileService = fileService;
    }

    @GetMapping("/add-global-account")
    public RedirectView linkedinAuth() {
        return linkedInService.authenticateGlobalAccount();
    }

    @GetMapping("/callback")
    public ResponseEntity<Object> linkedinCallback(@RequestParam("code") String code) {
        return linkedInService.authenticateGlobalAccountCallback(code);
    }

    @GetMapping("/{accountId}/organizations")
    public ResponseEntity<Object> getAdministeredPages(@PathVariable Long accountId) {
        try {
            return ResponseEntity.ok(linkedInService.fetchAdministeredPages(accountId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching administered pages: " + e.getMessage());
        }
    }

    @GetMapping("/{accountId}/{organizationId}/")
    public ResponseEntity<Object> getOrganizationDetails(@PathVariable Long accountId, @PathVariable String organizationId) {
        try {
            return ResponseEntity.ok(linkedInService.authenticateLinkedInOrganization(accountId, organizationId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching organization details: " + e.getMessage());
        }
    }

    @PostMapping("/{organizationId}/post")
    public ResponseEntity<Object> createPost(
            @PathVariable Long organizationId,
            @RequestParam List<MultipartFile> files,
            @RequestParam @Nullable Date scheduledAt,
            @RequestParam String content
    ) {
        try {
            List<FileData> fileDataList = files.stream().map((file) -> {
                try {
                    return fileService.processUploadedFile(file);
                } catch (Exception e) {
                    return null;
                }
            }).toList();
            PlatformPostResult result = linkedInService.createLinkedInPost(fileDataList, content, scheduledAt, organizationId);
            return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(PlatformPostResult.failure(PlatformType.LINKEDIN, e.getMessage()));
        }
    }

}
