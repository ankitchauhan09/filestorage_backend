package com.filestorageservice.storagemanagementservice.controller;

import com.filestorageservice.storagemanagementservice.entities.StorageFileInfo;
import com.filestorageservice.storagemanagementservice.payload.ApiResponse;
import com.filestorageservice.storagemanagementservice.service.service_interface.StorageFilesService;
import com.filestorageservice.storagemanagementservice.service.service_interface.StorageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manage-storage")
@Slf4j
@CrossOrigin(originPatterns = "*", origins = "http://localhost:3000", allowCredentials = "true")
public class StorageManagementController {

    @Autowired
    private StorageFilesService storageFilesService;

    @Autowired
    private StorageInfoService storageInfoService;

    @PostMapping("/add-files")
    public ResponseEntity<?> addFiles(@RequestBody List<StorageFileInfo> storageSummaryEntities) {
        log.info("storageSummaryEntities: {}", storageSummaryEntities);
        return ResponseEntity.ok().body(this.storageFilesService.addMultipleFiles(storageSummaryEntities));
    }

    @PostMapping("/add-file")
    public ResponseEntity<?> addFiles(@RequestBody StorageFileInfo storageSummaryEntity) {
        log.info("storageSummaryEntity: {}" ,storageSummaryEntity);
        return ResponseEntity.ok().body(this.storageFilesService.addSingleFile(storageSummaryEntity));
    }

    @GetMapping("/all-files")
    public ResponseEntity<?> allFiles(@RequestParam String userId) {
        return ResponseEntity.ok().body(ApiResponse.success(storageFilesService.getAllFiles(userId), "All Files"));
    }

    @GetMapping("/storage-info")
    public ResponseEntity<?> getStorageInfo(@RequestParam String userId) {
        return ResponseEntity.ok().body(ApiResponse.success(storageInfoService.getStorageInfo(userId), "Storage Info"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.info("error generated : {}", e.getMessage());
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
