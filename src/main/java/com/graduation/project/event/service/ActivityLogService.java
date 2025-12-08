package com.graduation.project.event.service;

import com.graduation.project.event.dto.ActivityLogResponse;
import com.graduation.project.event.dto.ActivityLogSearchRequest;
import com.graduation.project.event.entity.ActivityLog;
import com.graduation.project.event.repository.ActivityLogRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

  private final ActivityLogRepository activityLogRepository;

  public Page<ActivityLogResponse> searchLogs(ActivityLogSearchRequest request, Pageable pageable) {

    Specification<ActivityLog> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          // 1. Lọc theo User
          if (request.getUserId() != null) {
            predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
          }

          // 2. Lọc theo Module
          if (request.getModule() != null && !request.getModule().isEmpty()) {
            predicates.add(cb.equal(root.get("module"), request.getModule()));
          }

          // 3. Lọc theo Thời gian (Range)
          if (request.getFromDate() != null) {
            predicates.add(
                cb.greaterThanOrEqualTo(
                    root.get("createdAt"), request.getFromDate().atStartOfDay()));
          }
          if (request.getToDate() != null) {
            predicates.add(
                cb.lessThanOrEqualTo(
                    root.get("createdAt"), request.getToDate().atTime(23, 59, 59)));
          }

          // 4. Tìm kiếm từ khóa (Keyword) trong description hoặc action
          if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String likePattern = "%" + request.getKeyword().toLowerCase() + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("description")), likePattern),
                    cb.like(cb.lower(root.get("action")), likePattern)));
          }

          // Sắp xếp mặc định mới nhất lên đầu
          Objects.requireNonNull(query).orderBy(cb.desc(root.get("createdAt")));

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return activityLogRepository.findAll(spec, pageable).map(this::mapToResponse);
  }

  // Mapper function
  private ActivityLogResponse mapToResponse(ActivityLog entity) {
    ActivityLogResponse dto = new ActivityLogResponse();
    dto.setId(entity.getId());
    dto.setAction(entity.getAction());
    dto.setModule(entity.getModule());
    dto.setDescription(entity.getDescription());
    dto.setTargetId(entity.getTargetId());
    dto.setTargetType(entity.getTargetType());
    dto.setMetadata(entity.getMetadata());
    dto.setIpAddress(entity.getIpAddress());
    dto.setCreatedAt(entity.getCreatedAt());

    if (entity.getUser() != null) {
      dto.setUserId(entity.getUser().getId());
      dto.setUsername(entity.getUser().getEmail()); // Hoặc getFullName()
      // dto.setUserAvatar(...)
    }
    return dto;
  }
}
