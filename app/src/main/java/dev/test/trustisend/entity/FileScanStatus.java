package dev.test.trustisend.entity;

public enum FileScanStatus {
    PENDING,
    CLEAN,
    INFECTED,
    ERROR
    ;

    @Override
    public String toString() {
        return switch (this) {
            case PENDING -> "PENDING";
            case CLEAN -> "CLEAN";
            case INFECTED -> "INFECTED";
            default -> "ERROR";
        };
    }
}
