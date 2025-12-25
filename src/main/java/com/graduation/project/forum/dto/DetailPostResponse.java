package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailPostResponse {
  // ===== Core =====
  private String id;
  private String title;
  private String excerpt; // NEW
  private String content;
  private UUID topicId;
  private PostStatus postStatus;

  // ===== Time =====
  private LocalDateTime createdDateTime;
  private LocalDateTime lastModifiedDateTime;
  private LocalDateTime approvedAt;

  // ===== Legacy =====
  private UUID createdById;
  private Long reactionCount;
  private Boolean isDeleted;
  private List<String> urls;

  // ===== Existing flags (legacy UI) =====
  private boolean isPostCreator;
  private boolean canManageTopic;

  // ===== NEW fields =====
  private PostAuthorResponse author;
  private PostStatsResponse stats;
  private PostUserStateResponse userState;
  private PostPermissionsResponse permissions;
  private List<AttachmentResponse> attachments;

  public static DetailPostResponse from(
      Post post,
      Map<UUID, List<String>> urlsByPostId,
      boolean canManageTopic,
      boolean isPostCreator
  // Các data NEW này em sẽ truyền thêm từ service khi implement:
  // PostAuthorResponse author,
  // PostStatsResponse stats,
  // PostUserStateResponse userState,
  // PostPermissionsResponse permissions,
  // List<AttachmentResponse> attachments
  ) {
    final List<String> urls = urlsByPostId.getOrDefault(post.getId(), Collections.emptyList());

    return DetailPostResponse.builder()
        .id(post.getId().toString())
        .title(post.getTitle())
        .content(post.getContent())
        .excerpt(buildExcerpt(post.getContent(), 150)) // NEW
        .topicId(post.getTopic().getId())
        .postStatus(post.getPostStatus())
        .createdDateTime(post.getCreatedDateTime())
        .lastModifiedDateTime(post.getLastModifiedDateTime())
        .approvedAt(post.getApprovedAt())
        .createdById(post.getAuthor() != null ? post.getAuthor().getId() : null)
        .reactionCount(post.getReactionCount())
        .isDeleted(Boolean.TRUE.equals(post.getDeleted()))
        .urls(urls)
        .canManageTopic(canManageTopic)
        .isPostCreator(isPostCreator)
        .author(null)
        .stats(PostStatsResponse.builder()
            .reactionCount(post.getReactionCount())
            .commentCount(null)
            .viewCount(null)
            .build())
        .userState(null)
        .permissions(null)
        .attachments(null)
        .build();
  }

  private static String buildExcerpt(String content, int maxLen) {
    if (content == null)
      return "";
    String plain = content.replaceAll("\\s+", " ").trim();
    if (plain.length() <= maxLen)
      return plain;
    return plain.substring(0, maxLen).trim() + "...";
  }
}
