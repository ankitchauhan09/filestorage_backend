package com.filestorageservice.storagemanagementservice.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    String message;
    Boolean success;
    T data;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(message, true, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>(message, false, null);
    }
}
