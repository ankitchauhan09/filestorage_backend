package com.filestorageservice.storagemanagementservice.repositories;

import com.filestorageservice.storagemanagementservice.entities.StorageFileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageFileRepo extends JpaRepository<StorageFileInfo, String> {
    List<StorageFileInfo> findAllByUserId(String userId);
    void deleteByBucketNameAndFileName(String bucketName, String fileName);
}
