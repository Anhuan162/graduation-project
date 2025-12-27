package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.CategoryType;
import com.graduation.project.forum.dto.CategoryRequest;
import com.graduation.project.forum.dto.CategoryResponse;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.mapper.CategoryMapper;
import com.graduation.project.forum.repository.CategoryRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;
  private final CurrentUserService currentUserService;
  private final StreamProducer streamProducer;

  @Transactional
  public CategoryResponse createCategory(CategoryRequest categoryRequest) {
    User user = currentUserService.getCurrentUserEntity();
    Category category = categoryMapper.toCategory(categoryRequest, user);
    categoryRepository.save(category);

    ActivityLogDTO activityLogDTO =
        ActivityLogDTO.from(
            user.getId(),
            "CREATE CATEGORY",
            "FORUM",
            ResourceType.CATEGORY,
            category.getId(),
            "User " + user.getEmail() + " created new category: " + category.getName(),
            "1270.0.0.1");

    EventEnvelope eventEnvelope =
        EventEnvelope.from(EventType.ACTIVITY_LOG, activityLogDTO, "CATEGORY");
    streamProducer.publish(eventEnvelope);
    return categoryMapper.toCategoryResponse(category);
  }

  public CategoryResponse getOne(UUID id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    return categoryMapper.toCategoryResponse(category);
  }

  public List<CategoryResponse> getAll() {
    return categoryRepository.findAll().stream().map(categoryMapper::toCategoryResponse).toList();
  }

  public CategoryResponse update(UUID id, CategoryRequest updateReq) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

    category.setName(updateReq.getName());
    category.setDescription(updateReq.getDescription());
    category.setCategoryType(Enum.valueOf(CategoryType.class, updateReq.getCategoryType()));

    categoryRepository.save(category);
    return categoryMapper.toCategoryResponse(category);
  }

  public void delete(UUID id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    categoryRepository.delete(category);
  }
}
