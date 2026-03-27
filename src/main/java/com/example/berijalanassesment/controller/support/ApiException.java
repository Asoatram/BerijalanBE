package com.example.berijalanassesment.controller.support;

import com.example.berijalanassesment.dto.common.ApiErrorDtos;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<ApiErrorDtos.ErrorDetail> details;

    public ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public ApiException(
        HttpStatus status,
        String code,
        String message,
        List<ApiErrorDtos.ErrorDetail> details
    ) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : details;
    }
}
