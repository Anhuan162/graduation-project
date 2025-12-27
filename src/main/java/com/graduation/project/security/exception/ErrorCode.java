package com.graduation.project.security.exception;

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
      1006, "Account not verified. Please verify your account.", HttpStatus.BAD_REQUEST),
  ANNOUNCEMENT_NOT_FOUND(1007, "Announcement not found", HttpStatus.NOT_FOUND),
  FACULTY_NOT_FOUND(1008, "Faculty not found", HttpStatus.NOT_FOUND),
  UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  ACCOUNT_VERIFIED(1010, "Account is already verified.", HttpStatus.NOT_FOUND),
  CLASS_CODE_EXISTED(1011, "Class code already exists", HttpStatus.NOT_FOUND),
  SUBJECT_NOT_FOUND(1012, "Subject not found", HttpStatus.NOT_FOUND),
  SEMESTER_NOT_FOUND(1013, "Semester not found", HttpStatus.NOT_FOUND),
  SUBJECT_REFERENCE_NOT_FOUND(1014, "Subject reference not found", HttpStatus.NOT_FOUND),
  STUDENT_CODE_NULL(1015, "Student code must be not null", HttpStatus.NOT_FOUND),
  CPA_PROFILE_NOT_FOUND(1016, "Cpa profile not found", HttpStatus.NOT_FOUND),
  GPA_PROFILE_NOT_FOUND(1017, "Gpa profile not found", HttpStatus.NOT_FOUND),
  CATEGORY_NOT_FOUND(1018, "Category not found", HttpStatus.NOT_FOUND),
  TOPIC_NOT_FOUND(1019, "Topic not found", HttpStatus.NOT_FOUND),
  POST_NOT_FOUND(1020, "Post not found", HttpStatus.NOT_FOUND),
  COMMENT_NOT_FOUND(1021, "Comment not found", HttpStatus.NOT_FOUND),
  TOPIC_MEMBER_NOT_FOUND(1022, "Topic member not found", HttpStatus.NOT_FOUND),
  TOPIC_MEMBER_EXISTED(1023, "Topic member existed", HttpStatus.NOT_FOUND),
  FILE_NOT_FOUND(1024, "File not found", HttpStatus.NOT_FOUND),
  INVALID_REPORT_TARGET(1025, "Invalid report target", HttpStatus.NOT_FOUND),
  REPORT_ALREADY_EXISTED(1026, "Report already existed", HttpStatus.NOT_FOUND),
  REPORT_NOT_FOUND(1027, "Report not found", HttpStatus.NOT_FOUND),
  FAILED_ATTEMPTS(1028, "Too many failed attempts", HttpStatus.BAD_REQUEST),
  SESSION_REST_PASSWORD_NOT_FOUND(1029, "Session reset password not found", HttpStatus.NOT_FOUND),
  SESSION_REST_PASSWORD_HAS_USED(1030, "Session reset password has used", HttpStatus.GONE),
  EMAIL_NOT_FOUND(1031, "email not found", HttpStatus.NOT_FOUND),
  CAN_NOT_SEND_EMAIL(1032, "Can't send email", HttpStatus.BAD_REQUEST),
  UUID_IS_INVALID(1033, "UUID is invalid", HttpStatus.BAD_REQUEST),
  INVALID_STUDENT_CODE(1034, "Student code is invalid", HttpStatus.BAD_REQUEST),
  INVALID_REQUEST(1035, "Invalid request", HttpStatus.BAD_REQUEST),
  UPDATE_FALSE(1036, "Update failed", HttpStatus.BAD_REQUEST),
  DOCUMENT_NOT_FOUND(1037, "Document not found", HttpStatus.NOT_FOUND),
  INVALID_CLASS_CODE(1038, "Class code is invalid", HttpStatus.BAD_REQUEST),
  INVALID_FACULTY_CODE(1039, "Faculty code is invalid", HttpStatus.BAD_REQUEST),
  UPLOAD_FILE_FAILED(1040, "Can not upload this file", HttpStatus.BAD_REQUEST),
  FACULTY_EXISTED(1041, "Faculty already existed", HttpStatus.BAD_REQUEST),
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
