package com.graduation.project.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  @Builder.Default
  int code = 1000;

  String message;
  T result;

  public static <T> ApiResponse<T> ok(T result) {
    return ApiResponse.<T>builder().result(result).build();
  }

  public static <T> ApiResponse<T> of(int code, String message, T result) {
    return ApiResponse.<T>builder().code(code).message(message).result(result).build();
  }

  public static <T> ApiResponse<T> error(int code, String message) {
    return ApiResponse.<T>builder().code(code).message(message).build();
  }
}
