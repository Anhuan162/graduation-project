package com.graduation.project.forum.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.graduation.project.forum.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedPostEvent {
  private UUID postId;
  private UUID authorId;
  private String authorName;
  private String content;
  private UUID topicId;
  private LocalDateTime createdDateTime;

  public static CreatedPostEvent from(Post post) {
      return CreatedPostEvent.builder()
              .postId(post.getId())
              .authorId(post.getAuthor().getId())
              .authorName(post.getAuthor().getFullName())
              .content(post.getContent())
              .topicId(post.getTopic().getId())
              .build();
  }
}
