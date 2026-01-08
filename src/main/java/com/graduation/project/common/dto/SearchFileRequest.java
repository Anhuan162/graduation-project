package com.graduation.project.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.graduation.project.common.constant.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class SearchFileRequest {
  @JsonProperty("folder")
  private String folder;

  @JsonProperty("resourceType")
  private ResourceType resourceType;

  @JsonProperty("resourceId")
  private UUID resourceId;

  @JsonProperty("toDate")
  LocalDateTime toDate;

  @JsonProperty("fromDate")
  LocalDateTime fromDate;
}
