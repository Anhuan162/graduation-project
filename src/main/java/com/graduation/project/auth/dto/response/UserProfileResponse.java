package com.graduation.project.auth.dto.response;


import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.constant.Provider;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    private String email;
    private String fullName;
    private Boolean enabled;
    private String avatar_url;
    private String phone;
    private String studentCode;
    private String classCode;
    private String facultyName;
    private List<String> permissionResponse;
}
