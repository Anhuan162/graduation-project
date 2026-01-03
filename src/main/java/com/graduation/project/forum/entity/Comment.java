package com.graduation.project.forum.entity;

import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "root_comment_id")
  private Comment rootComment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reply_to_user_id")
  private User replyToUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @OneToMany(mappedBy = "parent")
  private List<Comment> replies = new ArrayList<>();

  @Column(columnDefinition = "TEXT")
  private String content;

  @Builder.Default
  private Instant createdDateTime = Instant.now();

  @Builder.Default
  private boolean deleted = false;

  @Column(name = "reaction_count")
  @Builder.Default
  private Long reactionCount = 0L;
}