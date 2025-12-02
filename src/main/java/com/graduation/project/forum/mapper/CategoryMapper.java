package com.graduation.project.forum.mapper;

import com.graduation.project.forum.entity.Category;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.CategoryRequest;
import com.graduation.project.forum.dto.CategoryResponse;
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
