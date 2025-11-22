package com.graduation.project.user.mapper;

import com.graduation.project.common.entity.Category;
import com.graduation.project.common.entity.Topic;
import com.graduation.project.common.entity.User;
import com.graduation.project.user.dto.TopicRequest;
import com.graduation.project.user.dto.TopicResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TopicMapper {
  @Mapping(target = "createdBy", source = "user")
  @Mapping(target = "posts", ignore = true)
  @Mapping(target = "category", source = "category")
  @Mapping(target = "id", ignore = true) // ignore id vì nó tự generate
  Topic toTopic(TopicRequest request, Category category, User user);

  @Mapping(target = "categoryId", source = "category.id")
  @Mapping(target = "createdBy", source = "createdBy.id")
  TopicResponse toTopicResponse(Topic topic);
}
