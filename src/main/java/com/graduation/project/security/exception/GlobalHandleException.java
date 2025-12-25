package com.graduation.project.security.exception;

import com.graduation.project.auth.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalHandleException {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());
    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), "Invalid email or password"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    ErrorCode errorCode = ErrorCode.FORBIDDEN;
    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), message));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    ErrorCode errorCode = ErrorCode.BAD_REQUEST;
    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handlingException(Exception ex) {
    log.error("Unhandled exception: ", ex);
    ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), "Internal server error"));
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
    ErrorCode errorCode = exception.getErrorCode();

    String message = exception.getMessage() != null ? exception.getMessage() : errorCode.getMessage();

    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), message));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    ErrorCode errorCode = ErrorCode.BAD_REQUEST;
    String param = ex.getName();
    String value = ex.getValue() == null ? "null" : ex.getValue().toString();

    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), "Invalid value for '" + param + "': " + value));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException ex) {
    ErrorCode errorCode = ErrorCode.BAD_REQUEST;
    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(ApiResponse.error(errorCode.getCode(), "Invalid request body"));
  }

}
