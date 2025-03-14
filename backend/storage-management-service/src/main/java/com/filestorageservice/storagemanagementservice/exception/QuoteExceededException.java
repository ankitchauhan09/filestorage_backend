package com.filestorageservice.storagemanagementservice.exception;

public class QuoteExceededException extends RuntimeException {
    public QuoteExceededException(String message) {
        super(message);
    }
}
