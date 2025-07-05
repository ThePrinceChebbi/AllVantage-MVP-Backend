package com.MarketingMVP.AllVantage.Services.FileData;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.FileData.FileDataRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
    public FileData processUploadedFile(@NotNull final MultipartFile file) throws IOException {
        String type = Objects.requireNonNull(file.getContentType()).contains("video") ? "video" : "image";
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
    public FileData getFileDataById(long fileDataId) throws ResourceNotFoundException
    {
        return fileDataRepository.fetchFileDataById(fileDataId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("The file with ID : %s could not be found.", fileDataId)));
    }

    @Override
    public ResponseEntity<byte[]> downloadImage(final @NotNull String filePath) throws IOException {
        byte[] file = Files.readAllBytes(new File(filePath).toPath());
        HttpHeaders headers = new HttpHeaders();
        String contentType = determineContentType(filePath);
        headers.setContentDispositionFormData("attachment", filePath);
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }

    private ResponseEntity<byte[]> getVideo(String path, HttpHeaders headers) throws IOException {
        final Path filePath = Paths.get(path).toAbsolutePath().normalize();
        File file = new File(filePath.toUri());
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

            if (fileData.getType().equalsIgnoreCase("video")) {
                return getVideo(fileData.getPath(), headers);
            }else {
                return downloadImage(fileData.getPath());
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    @Transactional
    public void deleteFileFromFileSystem(@NotNull final FileData fileData) throws IOException {
        Path filePath = Paths.get(fileData.getPath());
        Files.delete(filePath);
        fileDataRepository.deleteFileDataById(fileData.getId());
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
    public ResponseEntity<byte[]> getFileByName(String name) {
        try {
            String fullPath = FILE_SYSTEM_PATH + name;
            return downloadImage(fullPath);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public File getFileFromFileData(FileData fileData) {
        Path filePath = Paths.get(fileData.getPath());
        return new File(filePath.toUri());
    }

    @Override
    public byte[] getFileBytesByFileData(FileData fileData) throws IOException {
        return Files.readAllBytes(new File(fileData.getPath()).toPath());
    }

    @Override
    public ResponseEntity<byte[]> getThumbnail(Long fileId) {
        FileData fileData = getFileDataById(fileId);
        String videoPath = fileData.getPath();
        String thumbnailPath = videoPath + "_thumb.jpg";

        try {
            // Make sure the file exists before trying anything
            if (!Files.exists(Paths.get(videoPath))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Run FFmpeg to extract the first frame
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", videoPath, "-ss", "00:00:01", "-vframes", "1", thumbnailPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                // log error output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    reader.lines().forEach(System.err::println);
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            // Make sure the output was created
            if (!Files.exists(Paths.get(thumbnailPath))) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(Paths.get(thumbnailPath));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // important for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<Object> getAllFiles(int pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 10);
            List<FileData> fileDataList = fileDataRepository.fetchAllFileDatas(pageable);
            if (fileDataList.isEmpty()){
                return getAllFiles(0);
            }
            return ResponseEntity.ok(fileDataList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
