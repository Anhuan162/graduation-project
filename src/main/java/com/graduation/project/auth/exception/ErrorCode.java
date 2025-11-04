package com.graduation.project.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED(1001, "You don't have permission", HttpStatus.UNAUTHORIZED),
  USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
  INVALID_TOKEN(1004, "Token is invalid", HttpStatus.NOT_FOUND),
  TOKEN_EXPIRED(1005, "Token expired", HttpStatus.NOT_FOUND),
  ACCOUNT_NOT_VERIFIED(
      1006, "Account not verified. Please verify your account.", HttpStatus.NOT_FOUND),
  ANNOUNCEMENT_NOT_FOUND(1007, "Announcement not found", HttpStatus.NOT_FOUND),
  FACULTY_NOT_FOUND(1008, "Faculty not found", HttpStatus.NOT_FOUND),
  UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  ACCOUNT_VERIFIED(1010, "Account is already verified.", HttpStatus.NOT_FOUND),
  CLASS_CODE_EXISTED(1011, "Class code already exists", HttpStatus.NOT_FOUND),
  CPA_PROFILE_NAME_EXISTED(1012, "Cpa profile name already exists", HttpStatus.NOT_FOUND),
  SEMESTER_NOT_FOUND(1012, "Semester not found", HttpStatus.NOT_FOUND),
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
