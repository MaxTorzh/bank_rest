package com.example.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private HttpStatus status;
    private String reason;
    private String message;
    private List<String> errors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ApiError(HttpStatus status, String reason, String message, String stackTrace) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.errors = List.of(stackTrace);
        this.timestamp = LocalDateTime.now();
    }
}
