package com.graduation.project.forum.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
  private String id;
  private String title;
  private String content;
  private String topicId;
  private String createdById;
  private List<String> urls;
}
