package com.graduation.project.forum.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class SearchCommentRequest {
  private UUID authorId;
  private UUID postId;
  private Boolean deleted;
  private LocalDate fromDate;
  private LocalDate toDate;
}
