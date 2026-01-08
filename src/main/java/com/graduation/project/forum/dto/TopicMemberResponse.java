package com.graduation.project.forum.dto;

import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.constant.TopicRole;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicMemberResponse {
  private UUID id;
  private TopicRole topicRole;
  private boolean approved;
  private LocalDateTime joinedAt;
  private String fullName;
  private String avatarUrl;
  private String email;
  private UUID userId;
  private UUID topicId;

  public static TopicMemberResponse toTopicMemberResponse(TopicMember topicMember) {
    String fullName = topicMember.getUser().getFullName();
    if (fullName == null || fullName.isEmpty()) {
      fullName = topicMember.getUser().getEmail();
    }

    return TopicMemberResponse.builder()
        .id(topicMember.getId())
        .topicRole(topicMember.getTopicRole())
        .topicId(topicMember.getTopic().getId())
        .approved(topicMember.isApproved())
        .joinedAt(topicMember.getJoinedAt())
        .userId(topicMember.getUser().getId())
        .fullName(fullName)
        .avatarUrl(topicMember.getUser().getAvatarUrl())
        .email(topicMember.getUser().getEmail())
        .build();
  }
}
