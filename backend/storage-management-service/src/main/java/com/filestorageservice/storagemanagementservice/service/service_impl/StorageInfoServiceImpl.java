package com.filestorageservice.storagemanagementservice.service.service_impl;

import com.filestorageservice.storagemanagementservice.entities.StorageSummary;
import com.filestorageservice.storagemanagementservice.repositories.StorageInfoRepo;
import com.filestorageservice.storagemanagementservice.service.service_interface.StorageInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageInfoServiceImpl implements StorageInfoService {

    @Autowired
    private StorageInfoRepo storageInfoRepo;

    @Override
    public StorageSummary getStorageInfo(String userId) {
        try {
            return storageInfoRepo.findById(userId).orElseThrow(() -> new RuntimeException("No storage info found for user with user id : " + userId));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
