package com.graduation.project.forum.mapper;

import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.TopicRequest;
import com.graduation.project.forum.dto.TopicResponse;
import com.graduation.project.forum.entity.TopicMember;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class TopicMapper {
  @Mapping(target = "createdBy", source = "user")
  @Mapping(target = "posts", ignore = true)
  @Mapping(target = "category", source = "category")
  @Mapping(target = "id", ignore = true) // ignore id vì nó tự generate
  public abstract Topic toTopic(TopicRequest request, Category category, User user);

  @Mapping(target = "categoryName", source = "category.name")
  @Mapping(target = "createdBy", source = "createdBy.fullName")
  public abstract TopicResponse toTopicResponse(Topic topic);

  @AfterMapping
  protected void calculateCounts(Topic topic, @MappingTarget TopicResponse.TopicResponseBuilder response) {
    System.out.println("DEBUG: Calculating counts for Topic: " + topic.getTitle());
    if (topic.getPosts() != null) {
      long approvedPostCount = topic.getPosts().stream()
          .filter(post -> post.getPostStatus() == com.graduation.project.forum.constant.PostStatus.APPROVED)
          .count();
      System.out.println("DEBUG: Posts size: " + topic.getPosts().size() + ", Approved Posts: " + approvedPostCount);
      response.approvedPostCount(approvedPostCount);
    } else {
      System.out.println("DEBUG: Posts list is NULL");
      response.approvedPostCount(0);
    }

    if (topic.getTopicMembers() != null) {
      long memberCount = topic.getTopicMembers().stream()
          .filter(TopicMember::isApproved)
          .count();
      System.out
          .println("DEBUG: Members size: " + topic.getTopicMembers().size() + ", Approved Members: " + memberCount);
      response.memberCount(memberCount);
    } else {
      System.out.println("DEBUG: Members set is NULL");
      response.memberCount(0);
    }
  }
}
