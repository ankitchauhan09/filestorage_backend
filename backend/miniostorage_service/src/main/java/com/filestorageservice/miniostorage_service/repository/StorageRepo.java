package com.filestorageservice.miniostorage_service.repository;

import com.filestorageservice.miniostorage_service.entities.StorageSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepo extends JpaRepository<StorageSummaryEntity, String> {

    void deleteByBucketNameAndFileName(String bucketName, String fileName);
}
