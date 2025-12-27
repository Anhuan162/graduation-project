package com.graduation.project.forum.dto;

import java.time.Instant;

import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Topic;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Builder
@Data
public class DetailTopicResponse {
  private String categoryName;
  private String title;
  private String content;
  private Instant createdAt;
  private Instant lastModifiedAt;
  private TopicVisibility topicVisibility;
  private boolean isDeleted;
  private String createdBy;
  private CurrentUserContext currentUserContext;

  public static DetailTopicResponse from(Topic topic, CurrentUserContext currentUserContext) {
    return DetailTopicResponse.builder()
        .categoryName(topic.getCategory().getName())
        .title(topic.getTitle())
        .content(topic.getContent())
        .createdAt(topic.getCreatedAt())
        .lastModifiedAt(topic.getLastModifiedAt())
        .topicVisibility(topic.getTopicVisibility())
        .currentUserContext(currentUserContext)
        .isDeleted(topic.getDeleted())
        .build();
  }

  @Builder
  @Value
  public static class CurrentUserContext {
    boolean isTopicManager;
    boolean isTopicCreator;
    boolean isTopicMember;

    public static CurrentUserContext from(
        boolean isTopicCreator, boolean isTopicMember, boolean isTopicManager) {
      return CurrentUserContext.builder()
          .isTopicCreator(isTopicCreator)
          .isTopicMember(isTopicMember)
          .isTopicManager(isTopicManager)
          .build();
    }
  }
}
