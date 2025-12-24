package com.graduation.project.auth.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileUpdateRequest {

    String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ")
    String phone;

    String studentCode;
    String classCode;

}
