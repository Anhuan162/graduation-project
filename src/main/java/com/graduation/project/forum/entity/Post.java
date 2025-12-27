package com.graduation.project.forum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.PostStatus;
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
@Table(name = "posts")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Builder.Default
  private Instant createdDateTime = Instant.now();

  @Builder.Default
  private Instant lastModifiedDateTime = Instant.now();

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private PostStatus postStatus = PostStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by")
  @JsonIgnore
  private User approvedBy;

  private Instant approvedAt;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id")
  private Topic topic;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @Column(name = "reaction_count")
  @Builder.Default
  private Long reactionCount = 0L;

  @Builder.Default
  private boolean deleted = false;

  @PreUpdate
  public void onUpdate() {
    this.lastModifiedDateTime = Instant.now();
  }

  @Column(unique = true)
  private String slug;
}