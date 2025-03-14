package com.filestorageservice.storagemanagementservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "storage_summary")
public class StorageSummary {
    @Id
    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private String userId;

    @Column(name = "total_storage_size")
    private Long totalStorageSize;

    @Column(name = "storage_used")
    private Long storageUsed;

    @Column(name = "storage_plan_id")
    private String storagePlanId;

    @Column(name = "plan_type")
    private String planType;

    @Column(name = "no_of_files_stored")
        private Long noOfFilesStored;
}
