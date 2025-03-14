package com.filestorageservice.storagemanagementservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "storage_file_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageFileInfo {
    @Id
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "userId")
    private String userId;
    @Column(name = "bucket_name")
    private String bucketName;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "date")
    private LocalDateTime date;
}
