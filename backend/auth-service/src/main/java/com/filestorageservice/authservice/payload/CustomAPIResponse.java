package com.filestorageservice.authservice.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomAPIResponse {
    private String message;
}
