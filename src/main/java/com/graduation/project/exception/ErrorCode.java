package com.graduation.project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED(1001, "You don't have permission", HttpStatus.UNAUTHORIZED),
  USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
  ;

  private final int code;
  private final String message;
  private final HttpStatusCode httpStatusCode;

  ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
    this.code = code;
    this.message = message;
    this.httpStatusCode = httpStatusCode;
  }
}
