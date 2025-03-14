package com.filestorageservice.storagemanagementservice.repositories;

import com.filestorageservice.storagemanagementservice.entities.StorageSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageInfoRepo extends JpaRepository<StorageSummary, String> {

}
