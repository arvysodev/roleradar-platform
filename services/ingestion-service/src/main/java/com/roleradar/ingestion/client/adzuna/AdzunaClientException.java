package com.roleradar.ingestion.client.adzuna;

public class AdzunaClientException extends RuntimeException {
    public AdzunaClientException(String message, Throwable cause) {
        super(message, cause);
    }
}