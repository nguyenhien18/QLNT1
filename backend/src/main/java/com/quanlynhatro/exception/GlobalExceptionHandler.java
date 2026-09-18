package com.quanlynhatro.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.enums.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        if (exception.getErrorCode() != null) {
            ErrorCode errorCode = exception.getErrorCode();
            return ResponseEntity.status(errorCode.getStatusCode())
                    .body(ApiResponse.<Void>builder()
                            .code(errorCode.getCode())
                            .message(errorCode.getMessage())
                            .build());
        }

        HttpStatus status = exception.getStatus() == null ? HttpStatus.BAD_REQUEST : exception.getStatus();
        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .code(status.value())
                        .message(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_KEY.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.INVALID_KEY.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ErrorCode.UNAUTHORIZED.getMessage())
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity.status(ErrorCode.DATA_INTEGRITY_VIOLATION.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.DATA_INTEGRITY_VIOLATION.getCode())
                        .message(ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception occurred", exception);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                        .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                        .build());
    }
}
