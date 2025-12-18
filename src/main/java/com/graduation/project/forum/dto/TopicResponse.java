package com.graduation.project.forum.dto;

import java.time.LocalDateTime;

import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Topic;
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

  public static TopicResponse from(Topic topic) {
      return TopicResponse.builder()
              .categoryName(topic.getCategory().getName())
              .title(topic.getTitle())
              .content(topic.getContent())
              .createdAt(topic.getCreatedAt())
              .lastModifiedAt(topic.getLastModifiedAt())
              .topicVisibility(topic.getTopicVisibility())
              .isDeleted(topic.getDeleted())
              .build();
  }
}
