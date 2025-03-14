package com.filestorageservice.storagemanagementservice.service.service_interface;

import com.filestorageservice.storagemanagementservice.entities.StorageSummary;

public interface StorageInfoService {

    public StorageSummary getStorageInfo(String userId);

}
