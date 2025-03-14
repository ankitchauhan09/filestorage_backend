package com.filestorageservice.miniostorage_service.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDto implements Serializable {
    private String transactionId;
    private String fileUrl;
    private String userId;
    private String bucketName;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String date;
}
