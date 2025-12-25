package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.request.SearchUserRequest;
import com.graduation.project.auth.dto.response.UserResponse;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.User;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    private static String escapeLikePattern(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim().toLowerCase();
        return trimmed.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(SearchUserRequest req, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
                String escaped = escapeLikePattern(req.getEmail());
                predicates.add(cb.like(
                        cb.lower(root.get("email")),
                        "%" + escaped + "%", '\\'));
            }

            if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
                String escaped = escapeLikePattern(req.getFullName());
                predicates.add(cb.like(
                        cb.lower(root.get("fullName")),
                        "%" + escaped + "%", '\\'));
            }

            if (req.getStudentCode() != null && !req.getStudentCode().trim().isEmpty()) {
                String escaped = escapeLikePattern(req.getStudentCode());
                predicates.add(cb.like(
                        cb.lower(root.get("studentCode")),
                        "%" + escaped + "%", '\\'));
            }

            if (req.getClassCode() != null && !req.getClassCode().trim().isEmpty()) {
                String escaped = escapeLikePattern(req.getClassCode());
                predicates.add(cb.like(
                        cb.lower(root.get("classCode")),
                        "%" + escaped + "%", '\\'));
            }

            if (req.getEnable() != null) {
                predicates.add(cb.equal(root.get("enabled"), req.getEnable()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(UserResponse::from);
    }

    @Transactional
    public void deleteUser(String userId) {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UUID_IS_INVALID);
        }

        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        userRepository.deleteById(id);
    }
}
