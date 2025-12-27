package com.graduation.project.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

  UNAUTHORIZED(1001, "Unauthorized", HttpStatus.UNAUTHORIZED),
  USER_EXISTED(1002, "User existed", HttpStatus.CONFLICT),
  USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
  INVALID_TOKEN(1004, "Invalid token", HttpStatus.UNAUTHORIZED),
  TOKEN_EXPIRED(1005, "Token expired", HttpStatus.UNAUTHORIZED),

  ACCOUNT_NOT_VERIFIED(1006, "Account not verified", HttpStatus.FORBIDDEN),
  ANNOUNCEMENT_NOT_FOUND(1007, "Announcement not found", HttpStatus.NOT_FOUND),
  FACULTY_NOT_FOUND(1008, "Faculty not found", HttpStatus.NOT_FOUND),
  UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  CLASS_CODE_EXISTED(1011, "Class code existed", HttpStatus.CONFLICT),
  SUBJECT_NOT_FOUND(1012, "Subject not found", HttpStatus.NOT_FOUND),
  SEMESTER_NOT_FOUND(1013, "Semester not found", HttpStatus.NOT_FOUND),
  SUBJECT_REFERENCE_NOT_FOUND(1014, "Subject reference not found", HttpStatus.NOT_FOUND),
  STUDENT_CODE_NULL(1015, "Student code is null", HttpStatus.BAD_REQUEST),

  CPA_PROFILE_NOT_FOUND(1016, "CPA profile not found", HttpStatus.NOT_FOUND),
  GPA_PROFILE_NOT_FOUND(1017, "GPA profile not found", HttpStatus.NOT_FOUND),
  CATEGORY_NOT_FOUND(1018, "Category not found", HttpStatus.NOT_FOUND),
  TOPIC_NOT_FOUND(1019, "Topic not found", HttpStatus.NOT_FOUND),
  POST_NOT_FOUND(1020, "Post not found", HttpStatus.NOT_FOUND),
  COMMENT_NOT_FOUND(1021, "Comment not found", HttpStatus.NOT_FOUND),

  TOPIC_MEMBER_NOT_FOUND(1022, "Topic member not found", HttpStatus.NOT_FOUND),
  TOPIC_MEMBER_EXISTED(1023, "Topic member existed", HttpStatus.CONFLICT),
  FILE_NOT_FOUND(1024, "File not found", HttpStatus.NOT_FOUND),

  INVALID_REPORT_TARGET(1025, "Invalid report target", HttpStatus.BAD_REQUEST),
  REPORT_ALREADY_EXISTED(1026, "Report already existed", HttpStatus.CONFLICT),
  REPORT_NOT_FOUND(1027, "Report not found", HttpStatus.NOT_FOUND),

  FAILED_ATTEMPTS(1028, "Failed attempts", HttpStatus.TOO_MANY_REQUESTS),
  SESSION_RESET_PASSWORD_NOT_FOUND(1029, "Reset password session not found", HttpStatus.NOT_FOUND),
  SESSION_RESET_PASSWORD_HAS_USED(1030, "Reset password session has already been used", HttpStatus.BAD_REQUEST),

  EMAIL_NOT_FOUND(1031, "Email not found", HttpStatus.NOT_FOUND),
  CAN_NOT_SEND_EMAIL(1032, "Can not send email", HttpStatus.SERVICE_UNAVAILABLE),
  UUID_IS_INVALID(1033, "UUID is invalid", HttpStatus.BAD_REQUEST),

  VALIDATION_ERROR(1034, "Validation error", HttpStatus.BAD_REQUEST),
  FORBIDDEN(1035, "Forbidden", HttpStatus.FORBIDDEN),
  BAD_REQUEST(1036, "Bad request", HttpStatus.BAD_REQUEST),
  CONFLICT(1037, "Conflict", HttpStatus.CONFLICT),
  ACTIVITY_LOG_NOT_FOUND(1038, "Activity log not found", HttpStatus.NOT_FOUND),
  INVALID_REQUEST(1040, "Invalid request", HttpStatus.BAD_REQUEST),
  FILE_UPLOAD_FAILED(1041, "File upload failed", HttpStatus.SERVICE_UNAVAILABLE),
  INVALID_PASSWORD(1042, "Invalid password", HttpStatus.BAD_REQUEST),
  INVALID_STUDENT_CODE(1043, "Invalid student code", HttpStatus.BAD_REQUEST),

  ACCOUNT_ALREADY_VERIFIED(1044, "Account already verified", HttpStatus.CONFLICT),
  CLASS_CODE_INVALID(1045, "Class code is invalid", HttpStatus.BAD_REQUEST),
  FACULTY_CODE_MISMATCH(1046, "Faculty code mismatch", HttpStatus.BAD_REQUEST),
  INVALID_PARENT_COMMENT(1047, "Invalid parent comment", HttpStatus.BAD_REQUEST),
  INVALID_POST_STATUS_TRANSITION(1048, "Invalid post status transition", HttpStatus.BAD_REQUEST),
  INVALID_STATUS_TRANSITION(1049, "Invalid status transition", HttpStatus.BAD_REQUEST),
  INVALID_CATEGORY_TYPE(1050, "Invalid category type", HttpStatus.BAD_REQUEST);

  private final int code;
  private final String message;
  private final HttpStatus httpStatusCode;

  ErrorCode(int code, String message, HttpStatus httpStatusCode) {
    this.code = code;
    this.message = message;
    this.httpStatusCode = httpStatusCode;
  }
}
