package com.graduation.project.user.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.Category;
import com.graduation.project.common.entity.CategoryType;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.CategoryRepository;
import com.graduation.project.user.dto.CategoryResponse;
import com.graduation.project.user.dto.CategoryRequest;
import com.graduation.project.user.mapper.CategoryMapper;
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

  public CategoryResponse createCategory(CategoryRequest categoryRequest) {
    User user = currentUserService.getCurrentUserEntity();
    Category category = categoryMapper.toCategory(categoryRequest, user);
    categoryRepository.save(category);
    return categoryMapper.toCategoryResponse(category);
  }

  public CategoryResponse getOne(String id) {
    Category category =
        categoryRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Category not found"));
    return categoryMapper.toCategoryResponse(category);
  }

  public List<CategoryResponse> getAll() {
    return categoryRepository.findAll().stream().map(categoryMapper::toCategoryResponse).toList();
  }

  public CategoryResponse update(String id, CategoryRequest updateReq) {
    Category category =
        categoryRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Category not found"));

    category.setName(updateReq.getName());
    category.setDescription(updateReq.getDescription());
    category.setCategoryType(Enum.valueOf(CategoryType.class, updateReq.getCategoryType()));

    categoryRepository.save(category);
    return categoryMapper.toCategoryResponse(category);
  }

  public void delete(String id) {
    Category category =
        categoryRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Category not found"));
    categoryRepository.delete(category);
  }
}
