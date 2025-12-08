package com.graduation.project.auth.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchUserRequest {
  String email;
  String fullName;
  Boolean enable;
  String studentCode;
  String classCode;
}
