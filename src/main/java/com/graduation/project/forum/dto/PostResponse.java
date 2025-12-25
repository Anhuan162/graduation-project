package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import java.time.LocalDateTime;
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
  private String excerpt;
  private String content;
  private String slug;
  private PostStatus postStatus;
  private LocalDateTime createdDateTime;
  private LocalDateTime lastModifiedDateTime;
  private LocalDateTime approvedAt;
  private UUID topicId;
  private UUID createdById;
  private UUID approvedById;

  private Long reactionCount;
  private Boolean deleted;
  private List<String> urls;

  private PostAuthorResponse author;
  private PostStatsResponse stats;
  private PostUserStateResponse userState;
  private PostPermissionsResponse permissions;
  private List<AttachmentResponse> attachments;
}
