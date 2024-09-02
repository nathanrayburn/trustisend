package dev.test.trustisend.exception;

import lombok.Data;

@Data
public class InvalidTimestampFormatException extends RuntimeException{

    private final String message;

    public InvalidTimestampFormatException(String message) {
        super(message);
        this.message = message;
    }
}