package com.addon.rateLimit.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitException extends RuntimeException {
    protected HttpStatus status;


    public RateLimitException(String message) {
        super(message);

        this.status = HttpStatus.BAD_REQUEST;
    }

    public RateLimitException(String message, HttpStatus status) {
        super(message);

        this.status = status;
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);

        this.status = HttpStatus.BAD_REQUEST;
    }


}
