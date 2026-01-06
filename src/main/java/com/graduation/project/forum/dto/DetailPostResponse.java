package com.graduation.project.forum.dto;

import com.graduation.project.common.dto.FileMetadataResponse;
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
  private List<FileMetadataResponse> attachments;
  private boolean isPostCreator;
  private boolean canManageTopic;
  private Boolean isLiked;

  // Author info
  private Author author;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Author {
    private String id;
    private String fullName;
    private String avatarUrl;
    private String email;
  }

  public static DetailPostResponse from(
      Post post,
      Map<UUID, List<FileMetadataResponse>> filesByPostId,
      boolean canManageTopic,
      boolean isPostCreator,
      boolean isLiked) {

    // Build author info
    Author author = null;
    if (post.getAuthor() != null) {
      author = Author.builder()
          .id(post.getAuthor().getId().toString())
          .fullName(post.getAuthor().getFullName())
          .avatarUrl(post.getAuthor().getAvatarUrl())
          .email(post.getAuthor().getEmail())
          .build();
    }

    return DetailPostResponse.builder()
        .id(post.getId().toString())
        .title(post.getTitle())
        .content(post.getContent())
        .topicId(post.getTopic().getId())
        .createdDateTime(post.getCreatedDateTime())
        .lastModifiedDateTime(post.getLastModifiedDateTime())
        .postStatus(post.getPostStatus())
        .createdById(post.getAuthor() != null ? post.getAuthor().getId() : null)
        .approvedAt(post.getApprovedAt())
        .reactionCount(post.getReactionCount())
        .isDeleted(post.getDeleted())
        .attachments(filesByPostId.getOrDefault(post.getId(), Collections.emptyList()))
        .author(author)
        .canManageTopic(canManageTopic)
        .isPostCreator(isPostCreator)
        .isLiked(isLiked)
        .build();
  }
}
