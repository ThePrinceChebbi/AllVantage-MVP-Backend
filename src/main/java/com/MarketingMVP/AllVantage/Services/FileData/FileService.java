package com.MarketingMVP.AllVantage.Services.FileData;


import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {

    FileData processUploadedFile(@NotNull final MultipartFile file) throws IOException;

    String determineContentType(@NotNull String filePath);
    FileData getFileDataById(long fileDataId);

    ResponseEntity<byte[]> downloadImage(@NotNull String filePath) throws IOException;

    ResponseEntity<byte[]> getFile(final Long fileId, HttpHeaders headers);
    void deleteFileFromFileSystem(@NotNull final FileData fileData) throws IOException ;

    ResponseEntity<byte[]> getFileByName(String name);
    File getFileFromFileData(FileData fileData);

    byte[] getFileBytesByFileData(FileData fileData) throws IOException;

    ResponseEntity<byte[]> getThumbnail(Long fileId);

    ResponseEntity<Object> getAllFiles(int pageNumber);
}
