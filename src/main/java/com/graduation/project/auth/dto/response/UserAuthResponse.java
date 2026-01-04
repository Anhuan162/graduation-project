package com.graduation.project.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(exclude = { "email", "fullName", "permissions" })
public class UserAuthResponse {
    String id;
    String email;
    String fullName;
    String avatar;
    Set<String> permissions;

    String phone;
    String studentCode;
    String facultyName;
    String classCode;
}
