package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailPostResponse {
  private String id;
  private String title;
  private String content;
  private UUID topicId;
  private LocalDateTime createdDateTime;
  private LocalDateTime lastModifiedDateTime;
  private PostStatus postStatus;
  private UUID createdById;
  private LocalDateTime approvedAt;
  private Long reactionCount;
  private Boolean isDeleted;
  private List<String> urls;
  private boolean isPostCreator;
  private boolean canManageTopic;

  public static DetailPostResponse from(
      Post post,
      Map<UUID, List<String>> urlsByPostId,
      boolean canManageTopic,
      boolean isPostCreator) {
    return DetailPostResponse.builder()
        .id(post.getId().toString())
        .title(post.getTitle())
        .content(post.getContent())
        .topicId(post.getTopic().getId())
        .createdById(post.getAuthor() != null ? post.getAuthor().getId() : null)
        .urls(urlsByPostId.getOrDefault(post.getId(), Collections.emptyList()))
        .canManageTopic(canManageTopic)
        .isPostCreator(isPostCreator)
        .build();
  }
}
