package com.graduation.project.forum.entity;

import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private List<Comment> replies = new ArrayList<>();

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime createdDateTime = LocalDateTime.now();
  private boolean isDeleted;

  @Column(name = "reaction_count")
  private Long reactionCount = 0L;
}
