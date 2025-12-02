package com.graduation.project.forum.dto;

import lombok.Data;

@Data
public class TopicRequest {
  private String title;
  private String content;
  private String topicVisibility;
}
