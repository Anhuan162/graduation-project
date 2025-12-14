package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchPostRequest {
  private String title;
  private PostStatus postStatus;
  private String topicId;
  private UUID authorId;
  private LocalDateTime fromDate;
  private LocalDateTime toDate;
  private boolean isDeleted;
}
