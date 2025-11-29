package com.graduation.project.user.mapper;

import com.graduation.project.common.entity.Category;
import com.graduation.project.common.entity.User;
import com.graduation.project.user.dto.CategoryRequest;
import com.graduation.project.user.dto.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "creator", source = "user")
  @Mapping(target = "topics", ignore = true)
  Category toCategory(CategoryRequest request, User user);

  @Mapping(target = "createdById", source = "creator.id")
  CategoryResponse toCategoryResponse(Category category);
}
