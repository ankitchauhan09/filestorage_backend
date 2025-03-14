package com.filestorageservice.miniostorage_service.service.service_interface;

import com.filestorageservice.miniostorage_service.payload.FileMetadataDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface StorageService {
    public Boolean createBucket(String bucketName);
    public Map<String, String> uploadFile(String bucketName, MultipartFile file);
    public List<String> batchUploadFile(String bucketName, List<MultipartFile> files);
    public List<FileMetadataDto> getAllFiles(String bucketName);
    InputStream getFile(String bucketName, String fileName);

    Boolean deleteFile(String bucketName, String fileName);

}
