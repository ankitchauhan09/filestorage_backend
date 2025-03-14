package com.filestorageservice.miniostorage_service.controller;

import com.filestorageservice.miniostorage_service.exception.BucketAlreadyExistsException;
import com.filestorageservice.miniostorage_service.payload.ApiResponse;
import com.filestorageservice.miniostorage_service.payload.FileMetadataDto;
import com.filestorageservice.miniostorage_service.service.service_interface.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/storage")
@CrossOrigin(origins = "http://localhost:3000", originPatterns = "*", allowCredentials = "true")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/create-bucket")
    public ResponseEntity<?> createBucket(@RequestParam String bucketName) {
        return ResponseEntity.status(HttpStatus.OK).body(storageService.createBucket(bucketName));
    }

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(@RequestParam String bucketName, @RequestParam("file") MultipartFile file) {
        Map<String, String> data = storageService.uploadFile(bucketName, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("File uploaded successfully", data));
    }


    @PostMapping("/batch-upload")
    public ResponseEntity<?> batchUploadFile(@RequestParam String bucketName, @RequestParam("files") List<MultipartFile> files) {
        log.info("the batch files are : {}", files);
        List<String> batchUploadFile = storageService.batchUploadFile(bucketName, files);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Files uploaded successfully", batchUploadFile));
    }

    @GetMapping("/get-file")
    public ResponseEntity<Resource> getFile(@RequestParam String bucketName, @RequestParam String fileName) {
        try (InputStream inputStream = storageService.getFile(bucketName, fileName)) {
            if (inputStream == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            byte[] bytes = inputStream.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(bytes);

            // Determine content type
            String contentTypeStr = Files.probeContentType(Paths.get(fileName));
            MediaType contentType = (contentTypeStr != null) ? MediaType.parseMediaType(contentTypeStr) : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(contentType)
                    .contentLength(bytes.length)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFiles(@RequestParam String bucketName) {
        try {
            List<FileMetadataDto> fileList = storageService.getAllFiles(bucketName);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("All files fetched successfully", fileList));
        } catch (Exception e) {
            throw new RuntimeException("Error downloading files: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-file")
    public ResponseEntity<?> deleteFile(@RequestParam String bucketName, @RequestParam String fileName) {
        storageService.deleteFile(bucketName, fileName);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("File deleted successfully", true));
    }

    @ExceptionHandler(BucketAlreadyExistsException.class)
    public ResponseEntity<?> handleBucketAlreadyExists(BucketAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
    }

}
