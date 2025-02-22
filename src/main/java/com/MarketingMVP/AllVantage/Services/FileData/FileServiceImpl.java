package com.MarketingMVP.AllVantage.Services.FileData;


import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.FileData.FileDataRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{
    private final FileDataRepository fileDataRepository;
    public FileServiceImpl(FileDataRepository fileDataRepository)
    {
        this.fileDataRepository = fileDataRepository;
    }



    public FileData save(FileData fileData)
    {
        return fileDataRepository.save(fileData);
    }

    @Transactional
    public void deleteFileById(final long fileId)
    {
        FileData fileToDelete = getFileDataById(fileId);
        fileDataRepository.deleteFileDataById(fileId);
    }

    private final String  FILE_SYSTEM_PATH= Paths.get("").toAbsolutePath().resolve("src").resolve("main").resolve("resources").resolve("FileSystem").toString() + "/";

    @Override
    public FileData processUploadedFile(@NotNull final MultipartFile file, String type) throws IOException {
        var originalFileName = file.getOriginalFilename();
        assert originalFileName != null;
        var fileName = originalFileName.substring(0, originalFileName.indexOf('.'));
        var extension = originalFileName.substring(originalFileName.indexOf('.'));
        var filePath = Paths.get(FILE_SYSTEM_PATH)+"/" + fileName + UUID.randomUUID() + extension;
        FileData fileData = new FileData(filePath,type,extension);
        fileDataRepository.save(fileData);
        file.transferTo(new File(filePath));
        return fileData;
    }

    @Override
    public Path createNewCategoryDirectory(String dirName) throws IOException {
        Path path1 = Paths.get(FILE_SYSTEM_PATH).resolve(dirName).toAbsolutePath();
        Path path2 = Paths.get(FILE_SYSTEM_PATH).resolve("category-images").resolve(dirName).toAbsolutePath();
        if (!Files.exists(path1)) {
            Files.createDirectories(path1);
        }
        if (!Files.exists(path2)) {
            Files.createDirectories(path2);
        }
        return path1;
    }
    @Override
    public Path createNewEventDirectory(String dirName, String parentDirName) throws IOException {
        Path path = Paths.get(FILE_SYSTEM_PATH).resolve(parentDirName).resolve(dirName).toAbsolutePath();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    @Override
    public Path deleteEventDirectory(String dirName, String parentDirName) throws IOException {
        Path path = Paths.get(FILE_SYSTEM_PATH).resolve(parentDirName).resolve(dirName).toAbsolutePath();
        if (Files.exists(path)){
            deleteRecursively(path);
        }
        return path;
    }
    @Override
    public Path deleteCategoryDirectory(String dirName) throws IOException {
        Path path = Paths.get(FILE_SYSTEM_PATH).resolve(dirName).toAbsolutePath();
        if (Files.exists(path)){
            deleteRecursively(path);
        }
        return path;
    }
    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }


    @Override
    public FileData getFileDataById(long fileDataId) throws ResourceNotFoundException
    {
        return fileDataRepository.fetchFileDataById(fileDataId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The file with ID : %s could not be found.", fileDataId)));
    }

    private ResponseEntity<byte[]> downloadImage(@NotNull final  FileData fileData) throws IOException {
        final String filePath = fileData.getPath();
        byte[] file = Files.readAllBytes(new File(filePath).toPath());
        HttpHeaders headers = new HttpHeaders();
        String contentType = determineContentType(filePath);
        headers.setContentDispositionFormData("attachment", fileData.getPath());
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }

    private ResponseEntity<byte[]> getVideo(File file, HttpHeaders headers) throws IOException {
        Resource videoResource = new InputStreamResource(new FileInputStream(file));

        if (videoResource.exists() && videoResource.isReadable()) {
            byte[] videoBytes = StreamUtils.copyToByteArray(videoResource.getInputStream());
            HttpHeaders responseHeaders = new HttpHeaders();
            List<HttpRange> ranges = headers.getRange();
            if (ranges.isEmpty()) {
                responseHeaders.add("Content-Type", "video/mp4");
                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .body(videoBytes);
            } else {
                HttpRange range = ranges.get(0);
                long rangeStart = range.getRangeStart(videoBytes.length);
                long rangeEnd = range.getRangeEnd(videoBytes.length);
                responseHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + videoBytes.length);
                responseHeaders.add("Content-Type", "video/mp4");
                byte[] rangeBytes = new byte[(int) (rangeEnd - rangeStart + 1)];
                System.arraycopy(videoBytes, (int) rangeStart, rangeBytes, 0, rangeBytes.length);
                return ResponseEntity.status(206)
                        .headers(responseHeaders)
                        .body(rangeBytes);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<byte[]> getFile(final Long fileId, HttpHeaders headers) {
        try {
            FileData fileData = getFileDataById(fileId);
            final Path filePath = Paths.get(fileData.getPath()).toAbsolutePath().normalize();
            File file = new File(filePath.toUri());
            if (fileData.getType().equalsIgnoreCase("video")) {
                return getVideo(file, headers);
            }else {
                return downloadImage(fileData);
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    @Transactional
    public void deleteFileFromFileSystem(@NotNull final FileData fileData) throws IOException {
        Path filePath = Paths.get(fileData.getPath());
        try {
            Files.delete(filePath);
        } catch (NoSuchFileException e) {
            throw new IOException(String.format("File not found with ID: %d at path: %s", fileData.getId(), fileData.getPath()), e);
        } catch (AccessDeniedException e) {
            throw new IOException(String.format("Access denied when trying to delete file with ID: %d at path: %s", fileData.getId(), fileData.getPath()), e);
        } catch (IOException e) {
            throw new IOException(String.format("Failed to delete file with ID: %d at path: %s", fileData.getId(), fileData.getPath()), e);
        }
        fileDataRepository.deleteFileDataById(fileData.getId());
    }

    @Override
    @Transactional
    public void deleteAllFiles(@NotNull final List<FileData> files) throws IOException {
        for(FileData file : files)
        {
            File fileToDelete = new File(file.getPath());
            if(!fileToDelete.delete())
            {
                throw new IOException(String.format("Failed to delete file with file path : %s",file.getPath()));
            }
        }
        fileDataRepository.deleteAllFiles(files);
    }

    public String determineContentType(@NotNull String filePath) {

        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();

        HashMap<String, String> extensionToContentTypeMap = new HashMap<>();
        extensionToContentTypeMap.put("png", "image/png");
        extensionToContentTypeMap.put("jpg", "image/jpeg");
        extensionToContentTypeMap.put("jpeg", "image/jpeg");
        extensionToContentTypeMap.put("mp4", "video/mp4");
        return extensionToContentTypeMap.getOrDefault(extension, "application/octet-stream");
    }

    @Override
    public String getFileType(String fileName){
        String image = "Image";
        String video = "Video";

        HashMap<String, String > typesMap = new HashMap<>();
        typesMap.put("png", image);
        typesMap.put("jpg", image);
        typesMap.put("jpeg", image);
        typesMap.put("mp4", video);

        return typesMap.get(fileName);
    }

}
