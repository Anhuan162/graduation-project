package com.graduation.project.forum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.dto.CommentAcceptedResponse;
import com.graduation.project.forum.dto.PostAcceptedResonse;
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
@Table(name = "posts")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime createdDateTime = LocalDateTime.now();
  private LocalDateTime lastModifiedDateTime = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private PostStatus postStatus = PostStatus.PENDING;

  @ManyToOne
  @JoinColumn(name = "approved_by")
  @JsonIgnore
  private User approvedBy;

  private LocalDateTime approvedAt;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "topic_id")
  private Topic topic;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @Column(name = "reaction_count")
  private Long reactionCount = 0L;

  @Builder.Default
  private Boolean deleted = Boolean.FALSE;
  // @ManyToMany
  // @JoinTable(
  // name = "post_tags",
  // joinColumns = @JoinColumn(name = "post_id"),
  // inverseJoinColumns = @JoinColumn(name = "tag_id"))
  // private Set<Tag> tags = new HashSet<>();

  public PostAcceptedResonse toPostAcceptedResonse() {
    return PostAcceptedResonse.builder()
        .title(this.title)
        .postId(this.id)
        .content(this.content)
        .authorName(this.author != null ? this.author.getFullName() : "Unknown")
        .comments(this.comments.stream().map(comment -> {
          return CommentAcceptedResponse.builder()
              .commentId(comment.getId())
              .content(comment.getContent())
              .authorName(comment.getAuthor() != null ? comment.getAuthor().getFullName() : "Unknown")
              .build();
        }).toList())
        .build();
  }
}
