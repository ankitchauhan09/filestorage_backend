package com.filestorageservice.userservice.payload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse <T> {

    private String message;
    private T data;
    private Boolean success;

    public static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder().data(data).success(true).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message){
        return ApiResponse.<T>builder().data(null).success(false).message(message).build();
    }

}
