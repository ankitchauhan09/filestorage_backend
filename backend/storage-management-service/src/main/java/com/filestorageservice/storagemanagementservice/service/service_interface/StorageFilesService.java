package com.filestorageservice.storagemanagementservice.service.service_interface;

import com.filestorageservice.storagemanagementservice.entities.StorageFileInfo;

import java.util.List;

public interface StorageFilesService {

    public List<StorageFileInfo> addMultipleFiles(List<StorageFileInfo> storageSummaryEntities);
    public StorageFileInfo addSingleFile(StorageFileInfo storageSummaryEntity);

    List<StorageFileInfo> getAllFiles(String userId);
}
