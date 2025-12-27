package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
  private UUID id;
  private String title;
  private String content;

  private Instant createdDateTime;
  private Instant lastModifiedDateTime;
  private Instant approvedAt;

  private PostStatus postStatus;

  private Long reactionCount;

  private boolean deleted;

  private List<String> urls;
  private String slug;

  private UUID topicId;
  private UUID createdById;
  private UUID approvedById;

  private String excerpt;
  private PostAuthorResponse author;
  private PostStatsResponse stats;
  private PostUserStateResponse userState;
  private PostPermissionsResponse permissions;
  private List<AttachmentResponse> attachments;
}