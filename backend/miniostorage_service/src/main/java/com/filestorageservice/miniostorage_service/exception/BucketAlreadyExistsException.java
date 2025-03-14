package com.filestorageservice.miniostorage_service.exception;

import java.io.Serializable;

public class BucketAlreadyExistsException extends RuntimeException{
    public BucketAlreadyExistsException(String message) {
        super(message);
    }
}
