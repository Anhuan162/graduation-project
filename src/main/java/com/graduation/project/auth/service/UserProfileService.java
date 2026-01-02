package com.graduation.project.auth.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.auth.dto.request.UserProfileUpdateRequest;
import com.graduation.project.auth.dto.response.PublicUserProfileResponse;
import com.graduation.project.auth.dto.response.UserAuthResponse;
import com.graduation.project.auth.dto.response.UserProfileResponse;
import com.graduation.project.auth.dto.response.UserResponse;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FirebaseService;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.dao.DataAccessException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final com.graduation.project.auth.repository.UserRelationRepository userRelationRepository;
    private final CurrentUserService currentUserService;
    private final FirebaseService firebaseService;
    private final FacultyRepository facultyRepository;
    private final Validator validator;
    private final com.graduation.project.auth.mapper.UserAccessMapper userAccessMapper;

    private static final String AVATAR_FOLDER = "avatars";

    @Transactional(readOnly = true)
    public UserResponse getUser(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.UUID_IS_INVALID);
        }

        return UserResponse.from(
                userRepository.findById(uuid).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        UUID targetId;
        try {
            targetId = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.UUID_IS_INVALID);
        }

        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User currentUser = null;
        try {
            currentUser = currentUserService.getCurrentUserEntity();
        } catch (AppException e) {
            if (e.getErrorCode() != ErrorCode.UNAUTHENTICATED) {
                // Re-throw non-authentication exceptions to avoid hiding real errors
                throw e;
            }
            // Anonymous viewer - UNAUTHENTICATED exception is expected
        }

        var stats = userRepository.getUserStats(targetId);

        String facultyName = null;
        if (targetUser.getStudentCode() != null && targetUser.getClassCode() != null) {
            try {
                facultyName = getAndValidateFacultiesCode(targetUser.getStudentCode(), targetUser.getClassCode());
            } catch (AppException e) {
                log.warn("Validation error resolving faculty name for user {}: studentCode={}, classCode={}, error={}",
                        targetId, targetUser.getStudentCode(), targetUser.getClassCode(), e.getMessage());
            } catch (DataAccessException e) {
                log.error("Data access error resolving faculty name for user {}: studentCode={}, classCode={}",
                        targetId, targetUser.getStudentCode(), targetUser.getClassCode(), e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "Failed to resolve faculty name due to data access issue", e);
            } catch (Exception e) {
                log.error("Unexpected error resolving faculty name for user {}: studentCode={}, classCode={}",
                        targetId, targetUser.getStudentCode(), targetUser.getClassCode(), e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Failed to resolve faculty name", e);
            }
        }

        boolean isFollowing = false;
        if (currentUser != null && !currentUser.getId().equals(targetId)) {
            isFollowing = userRelationRepository.existsById(
                    new com.graduation.project.auth.entity.UserRelation.UserRelationId(currentUser.getId(), targetId));
        }

        return userAccessMapper.toResponse(targetUser, currentUser, stats, facultyName, isFollowing);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User user = currentUserService.getCurrentUserEntity();
        return getUserProfile(user.getId().toString());
    }

    @Transactional
    public UserProfileResponse updateProfileInfo(UserProfileUpdateRequest request) {
        Set<ConstraintViolation<UserProfileUpdateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (ConstraintViolation<UserProfileUpdateRequest> v : violations) {
                message.append(v.getMessage()).append("; ");
            }
            throw new AppException(ErrorCode.BAD_REQUEST, message.toString());
        }

        User user = currentUserService.getCurrentUserEntity();

        String facultyName = "";

        if (request.getClassCode() != null
                && !request.getClassCode().isBlank()
                && request.getStudentCode() != null
                && !request.getStudentCode().isBlank()) {

            facultyName = getAndValidateFacultiesCode(request.getStudentCode(), request.getClassCode());
            user.setStudentCode(request.getStudentCode());
            user.setClassCode(request.getClassCode());
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone().trim());
        }

        userRepository.save(user);

        UserProfileResponse response = user.toUserProfileResponse();
        if (!facultyName.isBlank()) {
            response.setFacultyName(facultyName);
        } else if (user.getStudentCode() != null && user.getClassCode() != null) {
            response.setFacultyName(getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode()));
        }
        return response;
    }

    @Transactional
    public UserProfileResponse updateAvatar(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Avatar image is required");
        }

        User user = currentUserService.getCurrentUserEntity();

        try {
            String oldAvatarStoragePath = user.getAvatarStoragePath();
            FirebaseService.FileUploadResult result = firebaseService.uploadFile(image, AVATAR_FOLDER);

            user.setAvatarUrl(result.url());
            user.setAvatarStoragePath(result.storagePath());
            try {
                userRepository.save(user);

                if (oldAvatarStoragePath != null && !oldAvatarStoragePath.isEmpty()) {
                    try {
                        firebaseService.deleteFile(oldAvatarStoragePath);
                    } catch (Exception e) {
                        log.warn("Failed to delete old avatar: {}", oldAvatarStoragePath, e);
                    }
                }
            } catch (Exception e) {
                // Save failed, delete the new file
                try {
                    firebaseService.deleteFile(result.storagePath());
                } catch (Exception deleteException) {
                    log.warn("Failed to delete uploaded avatar after save failure: {}", result.storagePath(),
                            deleteException);
                }
                // Rethrow the original exception, wrapping if necessary
                if (e instanceof AppException) {
                    throw e;
                } else {
                    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Failed to update user profile", e);
                }
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Upload avatar failed");
        }

        UserProfileResponse response = user.toUserProfileResponse();
        if (user.getStudentCode() != null && user.getClassCode() != null) {
            response.setFacultyName(getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode()));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public UserAuthResponse getAuthInfo() {
        UserPrincipal principal = currentUserService.getCurrentUserPrincipal();
        User user = currentUserService.getCurrentUserEntity();

        String facultyName = null;
        if (user.getStudentCode() != null && user.getClassCode() != null) {
            facultyName = getAndValidateFacultiesCode(user.getStudentCode(), user.getClassCode());
        }

        return UserAuthResponse.builder()
                .id(principal.getId().toString())
                .email(principal.getEmail())
                .fullName(principal.getFullName())
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : principal.getAvatar())
                .permissions(getUserPermissionsFromPrincipal(principal))
                .phone(user.getPhone())
                .studentCode(user.getStudentCode())
                .classCode(user.getClassCode())
                .facultyName(facultyName)
                .build();
    }

    public java.util.List<String> getPermissionOfCurrentUser() {
        UserPrincipal principal = currentUserService.getCurrentUserPrincipal();
        return new java.util.ArrayList<>(getUserPermissionsFromPrincipal(principal));
    }

    // ===== INTERNALS =====
    private Set<String> getUserPermissionsFromPrincipal(UserPrincipal principal) {
        Set<String> permissions = new HashSet<>();
        for (var auth : principal.getAuthorities()) {
            permissions.add(auth.getAuthority());
        }
        return permissions;
    }

    public String getAndValidateFacultiesCode(String studentCode, String classCode) {
        if (studentCode == null || classCode == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "studentCode/classCode is required");
        }

        String trimmedStudentCode = studentCode.trim().toUpperCase();
        String trimmedClassCode = classCode.trim().toUpperCase();

        if (trimmedStudentCode.length() != 10) {
            throw new AppException(ErrorCode.BAD_REQUEST, "studentCode is invalid");
        }
        if (trimmedClassCode.length() < 10) {
            throw new AppException(ErrorCode.BAD_REQUEST, "classCode is invalid");
        }

        String facultiesCodeFromStudent = trimmedStudentCode.substring(5, 7);
        String facultiesCodeFromClass = trimmedClassCode.substring(5, 7);

        if (!facultiesCodeFromStudent.equals(facultiesCodeFromClass)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "faculties code mismatch");
        }

        Optional<Faculty> faculty = facultyRepository.findByFacultyCode(facultiesCodeFromClass);
        if (faculty.isEmpty()) {
            throw new AppException(ErrorCode.FACULTY_NOT_FOUND);
        }
        return faculty.get().getFacultyName();
    }
}
