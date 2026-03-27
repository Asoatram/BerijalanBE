package com.example.berijalanassesment.controller.support;

import com.example.berijalanassesment.dto.common.ApiErrorDtos;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorDtos.ErrorEnvelope> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
            ApiErrorDtos.ErrorEnvelope.builder()
                .error(
                    ApiErrorDtos.ErrorPayload.builder()
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .details(ex.getDetails())
                        .traceId(UUID.randomUUID().toString())
                        .build()
                )
                .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDtos.ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorDtos.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorDtos.ErrorEnvelope.builder()
                .error(
                    ApiErrorDtos.ErrorPayload.builder()
                        .code("VALIDATION_ERROR")
                        .message("Request payload is invalid")
                        .details(details)
                        .traceId(UUID.randomUUID().toString())
                        .build()
                )
                .build()
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorDtos.ErrorEnvelope> handleDataAccess(DataAccessException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Data access error. traceId={}", traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiErrorDtos.ErrorEnvelope.builder()
                .error(
                    ApiErrorDtos.ErrorPayload.builder()
                        .code("DATABASE_ERROR")
                        .message("Database operation failed")
                        .details(List.of())
                        .traceId(traceId)
                        .build()
                )
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDtos.ErrorEnvelope> handleGeneric(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled error. traceId={}", traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiErrorDtos.ErrorEnvelope.builder()
                .error(
                    ApiErrorDtos.ErrorPayload.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("Unexpected server error")
                        .details(List.of())
                        .traceId(traceId)
                        .build()
                )
                .build()
        );
    }

    private ApiErrorDtos.ErrorDetail mapFieldError(FieldError error) {
        return ApiErrorDtos.ErrorDetail.builder()
            .field(error.getField())
            .rule(error.getCode())
            .message(error.getDefaultMessage())
            .build();
    }
}
