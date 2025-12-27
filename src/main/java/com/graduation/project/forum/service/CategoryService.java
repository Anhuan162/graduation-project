package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.CategoryType;
import com.graduation.project.forum.dto.CategoryRequest;
import com.graduation.project.forum.dto.CategoryResponse;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.mapper.CategoryMapper;
import com.graduation.project.forum.repository.CategoryRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;
  private final CurrentUserService currentUserService;
  private final StreamProducer streamProducer;

  @Transactional
  public CategoryResponse createCategory(CategoryRequest request) {
    User user = currentUserService.getCurrentUserEntity();

    Category category = categoryMapper.toCategory(request, user);
    Category savedCategory = categoryRepository.save(category);

    publishActivityLog(user, savedCategory, "CREATE");

    return categoryMapper.toCategoryResponse(savedCategory);
  }

  @Transactional(readOnly = true)
  public CategoryResponse getOne(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

    return categoryMapper.toCategoryResponse(category);
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getAll() {
    return categoryRepository.findAll().stream()
        .map(categoryMapper::toCategoryResponse)
        .toList();
  }

  @Transactional
  public CategoryResponse update(UUID id, CategoryRequest request) {
    User user = currentUserService.getCurrentUserEntity();

    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

    category.setName(request.getName());
    category.setDescription(request.getDescription());

    if (request.getCategoryType() == null) {
      throw new AppException(ErrorCode.INVALID_CATEGORY_TYPE);
    }
    try {
      category.setCategoryType(CategoryType.valueOf(request.getCategoryType()));
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.INVALID_CATEGORY_TYPE);
    }

    Category savedCategory = categoryRepository.save(category);

    publishActivityLog(user, savedCategory, "UPDATE");

    return categoryMapper.toCategoryResponse(savedCategory);
  }

  @Transactional
  public void delete(UUID id) {
    User user = currentUserService.getCurrentUserEntity();

    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

    publishActivityLog(user, category, "DELETE");

    categoryRepository.deleteById(id);
  }

  private void publishActivityLog(User user, Category category, String action) {
    try {
      ActivityLogDTO activityLogDTO = ActivityLogDTO.from(
          user.getId(),
          action + " CATEGORY",
          "FORUM",
          ResourceType.CATEGORY,
          category.getId(),
          String.format("User %s %s category: %s", user.getEmail(), action.toLowerCase(), category.getName()),
          resolveClientIp());

      EventEnvelope eventEnvelope = EventEnvelope.from(EventType.ACTIVITY_LOG, activityLogDTO, "CATEGORY");
      streamProducer.publish(eventEnvelope);
    } catch (Exception e) {
      log.error("Failed to publish activity log for category {}", category.getId(), e);
    }
  }

  private String resolveClientIp() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (!(requestAttributes instanceof ServletRequestAttributes servletAttributes)) {
      return "127.0.0.1";
    }

    HttpServletRequest request = servletAttributes.getRequest();
    String[] headers = {
        "X-Forwarded-For",
        "X-Real-IP",
        "CF-Connecting-IP",
        "X-Forwarded",
        "Forwarded-For",
        "Forwarded"
    };

    for (String header : headers) {
      String ipList = request.getHeader(header);
      if (ipList != null && !ipList.isBlank()) {
        return ipList.split(",")[0].trim();
      }
    }

    return request.getRemoteAddr();
  }
}
