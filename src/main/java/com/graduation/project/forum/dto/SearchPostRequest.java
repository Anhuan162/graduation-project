package com.graduation.project.forum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.graduation.project.forum.constant.PostStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchPostRequest {
  @JsonProperty("title")
  private String title;

  @JsonProperty("postStatus")
  private PostStatus postStatus;

  @JsonProperty("topicId")
  private String topicId;

  @JsonProperty("authorId")
  private UUID authorId;

  @JsonProperty("fromDate")
  private LocalDateTime fromDate;

  @JsonProperty("toDate")
  private LocalDateTime toDate;

  @JsonProperty("deleted")
  private Boolean deleted;
}
