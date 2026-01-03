package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;
import java.time.Instant;
import java.util.*;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailPostResponse {
  private UUID id;
  private String title;
  private String excerpt;
  private String content;
  private UUID topicId;
  private PostStatus postStatus;

  private Instant createdDateTime;
  private Instant lastModifiedDateTime;
  private Instant approvedAt;

  private UUID createdById;
  private Long reactionCount;
  private boolean deleted;
  private List<String> urls;

  private boolean isPostCreator;
  private boolean canManageTopic;

  private PostAuthorResponse author;
  private PostStatsResponse stats;
  private PostUserStateResponse userState;
  private PostPermissionsResponse permissions;
  private List<AttachmentResponse> attachments;

  public static DetailPostResponse from(
      Post post,
      Map<UUID, List<String>> urlsByPostId,
      boolean canManageTopic,
      boolean isPostCreator) {
    List<String> urls = urlsByPostId.getOrDefault(post.getId(), Collections.emptyList());

    return DetailPostResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .excerpt(buildExcerpt(post.getContent(), 150))
        .topicId(post.getTopic().getId())
        .postStatus(post.getPostStatus())

        .createdDateTime(post.getCreatedDateTime())
        .lastModifiedDateTime(post.getLastModifiedDateTime())
        .approvedAt(post.getApprovedAt())

        .createdById(post.getAuthor() != null ? post.getAuthor().getId() : null)
        .reactionCount(post.getReactionCount())
        .deleted(Boolean.TRUE.equals(post.isDeleted()))
        .urls(urls)
        .canManageTopic(canManageTopic)
        .isPostCreator(isPostCreator)

        .author(null)
        .stats(PostStatsResponse.builder()
            .reactionCount(post.getReactionCount() != null ? post.getReactionCount() : 0L)
            .build())
        .userState(null)
        .permissions(null)
        .attachments(null)
        .build();
  }

  private static String buildExcerpt(String content, int maxLen) {
    if (content == null || content.isEmpty())
      return "";
    String plain = content.replaceAll("\\s+", " ").trim();
    if (plain.length() <= maxLen)
      return plain;
    return plain.substring(0, maxLen).trim() + "...";
  }
}