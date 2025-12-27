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

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
    return buildResponse(ErrorCode.UNAUTHENTICATED, "Email hoặc mật khẩu không chính xác");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
    return buildResponse(ErrorCode.FORBIDDEN, null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    return buildResponse(ErrorCode.VALIDATION_ERROR, message);
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<?>> handlingAppException(AppException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    String message = ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage();
    return buildResponse(errorCode, message);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message = "Invalid value for parameter: " + ex.getName();
    return buildResponse(ErrorCode.BAD_REQUEST, message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException ex) {
    log.warn("Malformed JSON request: {}", ex.getMessage());
    return buildResponse(ErrorCode.BAD_REQUEST, "Malformed JSON request");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handlingException(Exception ex) {
    log.error("Uncategorized error: ", ex);
    return buildResponse(ErrorCode.UNCATEGORIZED_EXCEPTION, "Internal Server Error (Check logs)");
  }

  private ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode, String customMessage) {
    ApiResponse<?> apiResponse = ApiResponse.builder()
        .code(errorCode.getCode())
        .message(customMessage != null ? customMessage : errorCode.getMessage())
        .build();

    return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
  }
}
