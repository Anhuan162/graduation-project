package com.graduation.project.auth.dto.request;

import com.graduation.project.auth.validation.FileSize;
import com.graduation.project.auth.validation.FileType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    String fullName;

    @Nullable
    @FileSize(max = 5242880, message = "Kích thước file không được vượt quá 5MB")
    @FileType(allowed = { "image/jpeg", "image/png", "image/jpg" }, message = "Chỉ chấp nhận file ảnh")
    MultipartFile avatarFile;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ")
    String phone;

    String studentCode;
    String classCode;
    LocalDate dob;
}
