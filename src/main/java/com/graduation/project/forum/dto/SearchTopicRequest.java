package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.TopicVisibility;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchTopicRequest {
  private UUID categoryId;
  private String keyword;
  private TopicVisibility visibility;
  private LocalDate toDate;
  private LocalDate fromDate;
}
