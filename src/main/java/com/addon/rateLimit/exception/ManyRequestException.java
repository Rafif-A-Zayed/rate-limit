package com.addon.rateLimit.exception;

import org.springframework.http.HttpStatus;

public class ManyRequestException extends RateLimitException {

    public ManyRequestException(String message) {
        super(message);

        this.status = HttpStatus.TOO_MANY_REQUESTS;
    }


}
