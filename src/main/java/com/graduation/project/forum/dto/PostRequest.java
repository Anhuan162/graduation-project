package com.graduation.project.forum.dto;

import java.util.List;
import java.util.UUID;

import com.graduation.project.forum.constant.PostStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
  private String title;

  @NotBlank
  private String content;

  private PostStatus status;
  private List<UUID> fileMetadataIds;
}
