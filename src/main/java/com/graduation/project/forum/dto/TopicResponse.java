package com.graduation.project.forum.dto;

import java.time.LocalDateTime;

import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TopicResponse {
  private String id;
  private String categoryName;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime lastModifiedAt;
  private TopicVisibility topicVisibility;
  private boolean isDeleted;
  private String createdBy;
  private long approvedPostCount;
  private long memberCount;

  public static TopicResponse from(Topic topic) {
    long approvedPostCount = topic.getPosts().stream()
        .filter(post -> post.getPostStatus() == com.graduation.project.forum.constant.PostStatus.APPROVED)
        .count();

    long memberCount = topic.getTopicMembers().stream()
        .filter(TopicMember::isApproved)
        .count();

    return TopicResponse.builder()
        .id(topic.getId().toString())
        .categoryName(topic.getCategory().getName())
        .title(topic.getTitle())
        .content(topic.getContent())
        .createdAt(topic.getCreatedAt())
        .lastModifiedAt(topic.getLastModifiedAt())
        .topicVisibility(topic.getTopicVisibility())
        .isDeleted(topic.getDeleted())
        .createdBy(topic.getCreatedBy().getId().toString())
        .approvedPostCount(approvedPostCount)
        .memberCount(memberCount)
        .build();
  }
}
