package com.graduation.project.security.exception;

import com.graduation.project.auth.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalHandleException {

  @ExceptionHandler(value = RuntimeException.class)
  ResponseEntity<ApiResponse<?>> handlingRuntimeException(RuntimeException exception) {
    log.error("Exception: ", exception);
    ApiResponse<?> apiResponse = new ApiResponse<>();

    apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
    apiResponse.setMessage(exception.getMessage());

    return ResponseEntity.badRequest().body(apiResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handlingException(Exception ex) {
    log.error("Exception: ", ex);
    ApiResponse<?> apiResponse = new ApiResponse<>();

    apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
    apiResponse.setMessage(ex.getMessage());

    return ResponseEntity.badRequest().body(apiResponse);
  }

  @ExceptionHandler(value = AppException.class)
  ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    ApiResponse<?> apiResponse = new ApiResponse<>();

    apiResponse.setCode(errorCode.getCode());
    apiResponse.setMessage(errorCode.getMessage());

    return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
  }

  @ExceptionHandler(value = BadCredentialsException.class)
  ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());

    ApiResponse<?> apiResponse = new ApiResponse<>();
    apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
    apiResponse.setMessage("Invalid email or password");

    return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatusCode()).body(apiResponse);
  }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest()
                .body(apiResponse);
    }
}
