package com.example.bankcards.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final UserNotFoundException e) {
        log.error("404 Not Found: {}", e.getMessage(), e);
        String stackTrace = getStackTrace(e);
        return new ApiError(
                HttpStatus.NOT_FOUND,
                "The required user was not found",
                e.getMessage(),
                stackTrace
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException e) {
        log.error("400 Bad Request: {}", e.getMessage(), e);
        String stackTrace = getStackTrace(e);
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrect made request",
                e.getMessage(),
                stackTrace
        );
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
