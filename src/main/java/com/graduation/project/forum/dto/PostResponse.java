package com.graduation.project.forum.dto;

import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.dto.UserSummaryDto;
import com.graduation.project.forum.constant.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
  private String id;
  private String title;
  private String content;
  private String topicId;
  private UserSummaryDto author;
  private String approvedById;
  private LocalDateTime createdDateTime;
  private LocalDateTime approvedAt;
  private PostStatus postStatus;
  private Long reactionCount;
  private Boolean deleted;
  private List<FileMetadataResponse> attachments;
  private Boolean isLiked;
  private Boolean isSaved;
}
