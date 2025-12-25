package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
  private String id;
  private String title;
  private String excerpt;
  private String content;
  private String slug;
  private String topicId;
  private PostStatus postStatus;
  private LocalDateTime createdDateTime;
  private LocalDateTime lastModifiedDateTime;
  private LocalDateTime approvedAt;

  private String createdById;
  private String approvedById;
  private Long reactionCount;
  private Boolean deleted;
  private List<String> urls;

  private PostAuthorResponse author;
  private PostStatsResponse stats;
  private PostUserStateResponse userState;
  private PostPermissionsResponse permissions;
  private List<AttachmentResponse> attachments;
}
