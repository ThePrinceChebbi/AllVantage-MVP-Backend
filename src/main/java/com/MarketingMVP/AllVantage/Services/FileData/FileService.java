package com.MarketingMVP.AllVantage.Services.FileData;


import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {

    FileData processUploadedFile(@NotNull final MultipartFile file, String type) throws IOException;
    Path createNewEventDirectory (String dirName, String parentDirName) throws IOException;
    Path createNewCategoryDirectory (String dirName) throws IOException;
    Path deleteEventDirectory(String dirName, String parentDirName) throws IOException;
    Path deleteCategoryDirectory(String dirName) throws IOException;
    void deleteAllFiles(@NotNull final List<FileData> files) throws IOException;
    String determineContentType(@NotNull String filePath);
    FileData getFileDataById(long fileDataId);
    ResponseEntity<byte[]> getFile(final Long fileId, HttpHeaders headers);
    void deleteFileFromFileSystem(@NotNull final FileData fileData) throws IOException ;
    String getFileType(String fileName);

    ResponseEntity<byte[]> getFileByName(String name);
}
