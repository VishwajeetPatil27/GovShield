package com.govshield.exception;

public class CustomException extends RuntimeException {
    private String code;
    private int status;

    public CustomException(String message) {
        super(message);
        this.code = "INTERNAL_ERROR";
        this.status = 500;
    }

    public CustomException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
