package com.MarketingMVP.AllVantage.Controllers.Files;

import com.MarketingMVP.AllVantage.Services.FileData.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/get-file")
    public ResponseEntity<byte[]> getFileByPath(@RequestParam("name") String name) {
        return fileService.getFileByName(name);
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file")MultipartFile file) throws Exception {
        return ResponseEntity.ok(fileService.processUploadedFile(file, file.getContentType()));
    }
}
