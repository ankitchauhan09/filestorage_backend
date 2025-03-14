package com.filestorageservice.miniostorage_service.service.service_impl;

import com.filestorageservice.miniostorage_service.entities.StorageSummaryEntity;
import com.filestorageservice.miniostorage_service.exception.BucketAlreadyExistsException;
import com.filestorageservice.miniostorage_service.payload.ApiResponse;
import com.filestorageservice.miniostorage_service.payload.FileMetadataDto;
import com.filestorageservice.miniostorage_service.repository.StorageRepo;
import com.filestorageservice.miniostorage_service.service.service_interface.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private StorageRepo storageRepo;

    @Autowired
    private WebClient storageManagementWebClient;

    @Override
    public Boolean createBucket(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (found) {
                throw new BucketAlreadyExistsException("Bucket already exists with name: " + bucketName);
            }
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return true;
        } catch (BucketAlreadyExistsException e) {
            throw new BucketAlreadyExistsException("Bucket already exists with name: " + bucketName);
        } catch (Exception e) {
            log.error("Error while creating the bucket with name: {}", bucketName, e);
            throw new RuntimeException("Failed to create bucket: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "allFiles", key = "#bucketName")
    public Map<String, String> uploadFile(String bucketName, MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            saveFileUploadSummary(bucketName, fileName, file.getSize(), file.getContentType());
            String presignedUrl = generatePresignedUrl(bucketName, fileName);
            Map<String, String> result = new HashMap<>();
            result.put("url", presignedUrl);
            result.put("fileName", fileName);
            return result;
        } catch (Exception e) {
            log.error("Error while uploading file to bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private String generatePresignedUrl(String bucketName, String fileName) {
        try {
            // Generate a pre-signed URL valid for 7 days (example)
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET) // HTTP method (GET for downloading)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS) // URL expiry time (e.g., 365 days)
                            .build());
            return url;
        } catch (Exception e) {
            log.error("Error generating pre-signed URL for file: {}", fileName, e);
            throw new RuntimeException("Failed to generate pre-signed URL: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "allFiles", key = "#bucketName")
    public List<String> batchUploadFile(String bucketName, List<MultipartFile> files) {
        try {
            List<StorageSummaryEntity> filesToUpload = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                InputStream inputStream = file.getInputStream();

                // Upload directly to MinIO without calling uploadFile method
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

                String presignedUrl = generatePresignedUrl(bucketName, fileName);
                urls.add(presignedUrl);

                // Add to batch list instead of saving individually
                filesToUpload.add(StorageSummaryEntity.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .fileName(fileName)
                        .fileSize(file.getSize())
                        .bucketName(bucketName)
                        .userId(bucketName)
                        .contentType(file.getContentType())
                        .date(LocalDateTime.now())
                        .build());
            }

            // Save all the file metadata at once
            saveBatchFileUploadSummary(filesToUpload);
            return urls;
        } catch (Exception e) {
            log.error("Error while batch uploading files to bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to batch upload files: " + e.getMessage(), e);
        }
    }


    @Override
    @Cacheable(value = "allFiles", key = "#bucketName")
    public List<FileMetadataDto> getAllFiles(String bucketName) {
//        List<FileMetadataDto> objectMetadataList = new ArrayList<>();
//        try {
//            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
//                    .bucket(bucketName)
//                    .build());
//            for (Result<Item> result : results) {
//                Item item = result.get();
//                StatObjectResponse object = minioClient.statObject(StatObjectArgs.builder()
//                        .bucket(bucketName)
//                        .object(item.objectName())
//                        .build());
//                objectMetadataList.add(
//                        FileMetadataDto.builder()
//                                .name(item.objectName())
//                                .size(object.size())
//                                .contentType(object.contentType())
//                                .build()
//                );
//            }
//            return objectMetadataList;
        try {
            // Change the return type to match the wrapper structure
            ApiResponse<List<StorageSummaryEntity>> response = storageManagementWebClient.get()
                    .uri("/all-files?userId={userId}", bucketName)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<StorageSummaryEntity>>>() {})
                    .doOnError(error -> {
                        log.error("Error occurred while fetching files: {}", error.getMessage(), error);
                    })
                    .block();

            // Extract the actual data from the wrapper
            List<StorageSummaryEntity> storageSummaryEntities = response.getData();
            log.info("storageSummaryEntities size: {}", storageSummaryEntities);
            List<FileMetadataDto> fileMetadataDtos = new ArrayList<>();
            for (StorageSummaryEntity storageSummaryEntity : storageSummaryEntities) {
                String url = generatePresignedUrl(bucketName, storageSummaryEntity.getFileName());

                fileMetadataDtos.add(FileMetadataDto.builder()
                        .fileName(storageSummaryEntity.getFileName())
                        .fileSize(storageSummaryEntity.getFileSize())
                        .bucketName(storageSummaryEntity.getBucketName())
                        .userId(storageSummaryEntity.getUserId())
                        .date(storageSummaryEntity.getDate().toString())
                        .fileUrl(url)
                        .contentType(storageSummaryEntity.getContentType())
                        .transactionId(storageSummaryEntity.getTransactionId())
                        .build());
            }
            return fileMetadataDtos;
        } catch (Exception e) {
            log.error("Error while fetching files from bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to fetch files: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getFile(String bucketName, String fileName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            log.error("Error while fetching file {} from bucket: {}", fileName, bucketName, e);
            throw new RuntimeException("Failed to fetch file: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "allFiles", key = "#bucketName")
    public Boolean deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            storageRepo.deleteByBucketNameAndFileName(bucketName, fileName);
            return true;
        } catch (Exception e) {
            log.error("Error while deleting file: {} from bucket: {}", fileName, bucketName, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }


    void saveBatchFileUploadSummary(List<StorageSummaryEntity> storageSummaryEntities) {
        storageManagementWebClient.post()
                .uri("/add-files")
                .bodyValue(storageSummaryEntities)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("Error saving batch file summary", error))
                .subscribe();
    }

    void saveFileUploadSummary(String bucketName, String fileName, Long fileSize, String contentType) {
        storageManagementWebClient.post()
                .uri("/add-file")
                .bodyValue(StorageSummaryEntity.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .fileName(fileName)
                        .fileSize(fileSize)
                        .bucketName(bucketName)
                        .userId(bucketName)
                        .contentType(contentType)
                        .date(LocalDateTime.now())
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("Error saving file summary", error))
                .subscribe();
    }

}