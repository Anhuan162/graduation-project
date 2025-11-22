package com.graduation.project.user.dto;

import lombok.Data;

@Data
public class TopicRequest {
  private String title;
  private String content;
  private String createdBy;
  private String topicVisibility;
}
