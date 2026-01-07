package com.graduation.project.auth.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
public class SearchUserRequest {
  String email;
  String fullName;
  Boolean enable;
  String studentCode;
  String classCode;
}
