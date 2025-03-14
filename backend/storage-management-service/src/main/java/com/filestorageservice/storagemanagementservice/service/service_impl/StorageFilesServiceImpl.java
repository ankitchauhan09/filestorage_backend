package com.filestorageservice.storagemanagementservice.service.service_impl;

import com.filestorageservice.storagemanagementservice.entities.StorageFileInfo;
import com.filestorageservice.storagemanagementservice.entities.StorageSummary;
import com.filestorageservice.storagemanagementservice.exception.QuoteExceededException;
import com.filestorageservice.storagemanagementservice.repositories.StorageFileRepo;
import com.filestorageservice.storagemanagementservice.repositories.StorageInfoRepo;
import com.filestorageservice.storagemanagementservice.service.service_interface.StorageFilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StorageFilesServiceImpl implements StorageFilesService {

    @Autowired
    private StorageFileRepo storageRepo;

    @Autowired
    private StorageInfoRepo storageInfoRepo;
    @Autowired
    private StorageFileRepo storageFileRepo;

    @Override
    public List<StorageFileInfo> addMultipleFiles(List<StorageFileInfo> storageFileInfoList) {
        try {
            StorageSummary storageFileInfo = this.storageInfoRepo.findById(storageFileInfoList.get(0).getUserId()).orElseThrow(() -> new RuntimeException("Storage info not found in db"));
            Long totalSizeAvailable = storageFileInfo.getTotalStorageSize() - storageFileInfo.getStorageUsed();
            Long totalUploadSize = 0L;
            for (StorageFileInfo storageFile : storageFileInfoList) {
                totalUploadSize += storageFile.getFileSize();
            }
            if (totalSizeAvailable < totalUploadSize) {
                throw new QuoteExceededException("Total file size exceeded");
            }
            this.storageRepo.saveAll(storageFileInfoList);
            storageFileInfo.setNoOfFilesStored(storageFileInfo.getNoOfFilesStored() + storageFileInfoList.size());
            storageFileInfo.setStorageUsed(storageFileInfo.getStorageUsed() + totalUploadSize);
            this.storageInfoRepo.save(storageFileInfo);
            return storageFileInfoList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public StorageFileInfo addSingleFile(StorageFileInfo storageSummaryEntity) {
        try {
            log.info("storagefileInfo : {}", storageSummaryEntity);
            StorageSummary storageFileInfo = this.storageInfoRepo.findById(storageSummaryEntity.getUserId()).orElseThrow(() -> new RuntimeException("Storage info not found in db"));
            Long totalSizeAvailable = storageFileInfo.getTotalStorageSize() - storageFileInfo.getStorageUsed();
            Long totalUploadSize = storageSummaryEntity.getFileSize();
            if (totalSizeAvailable < totalUploadSize) {
                throw new QuoteExceededException("Total file size exceeded");
            }
            this.storageRepo.save(storageSummaryEntity);
            storageFileInfo.setNoOfFilesStored(storageFileInfo.getNoOfFilesStored() + 1);
            storageFileInfo.setStorageUsed(storageFileInfo.getStorageUsed() + totalUploadSize);
            this.storageInfoRepo.save(storageFileInfo);
            return storageSummaryEntity;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public List<StorageFileInfo> getAllFiles(String userId) {
        try {
            return storageFileRepo.findAllByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }


}
