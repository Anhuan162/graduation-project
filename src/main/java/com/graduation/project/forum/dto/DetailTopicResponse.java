package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Topic;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Builder
@Data
public class DetailTopicResponse {
  private String id;
  private String categoryName;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime lastModifiedAt;
  private TopicVisibility topicVisibility;
  private boolean isDeleted;
  private String createdBy;
  private CurrentUserContext currentUserContext;

  private long approvedPostCount;
  private long memberCount;

  public static DetailTopicResponse from(Topic topic, CurrentUserContext currentUserContext) {
    long approvedPostCount = topic.getPosts().stream()
        .filter(post -> post.getPostStatus() == com.graduation.project.forum.constant.PostStatus.APPROVED)
        .count();

    long memberCount = topic.getTopicMembers().stream()
        .filter(com.graduation.project.forum.entity.TopicMember::isApproved)
        .count();

    return DetailTopicResponse.builder()
        .id(topic.getId().toString())
        .categoryName(topic.getCategory().getName())
        .title(topic.getTitle())
        .content(topic.getContent())
        .createdAt(topic.getCreatedAt())
        .lastModifiedAt(topic.getLastModifiedAt())
        .topicVisibility(topic.getTopicVisibility())
        .currentUserContext(currentUserContext)
        .isDeleted(topic.getDeleted())
        .approvedPostCount(approvedPostCount)
        .memberCount(memberCount)
        .build();
  }

  @Builder
  @Value
  public static class CurrentUserContext {
    boolean isTopicManager;
    boolean isTopicCreator;
    boolean isTopicMember;
    String requestStatus; // "NONE", "PENDING", "APPROVED"

    public static CurrentUserContext from(
        boolean isTopicCreator, boolean isTopicMember, boolean isTopicManager, String requestStatus) {
      return CurrentUserContext.builder()
          .isTopicCreator(isTopicCreator)
          .isTopicMember(isTopicMember)
          .isTopicManager(isTopicManager)
          .requestStatus(requestStatus)
          .build();
    }
  }
}
