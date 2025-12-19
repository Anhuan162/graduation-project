package com.graduation.project.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UserProfileRequest {
    private String fullName;
    private MultipartFile avatarFile;
    private String phone;
    private String studentCode;
    private String classCode;
}

