package com.MarketingMVP.AllVantage.Controllers.Files;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getFileById(@PathVariable("fileId") Long fileId, @RequestHeader HttpHeaders headers) {
        return fileService.getFile(fileId, headers);
    }
}
