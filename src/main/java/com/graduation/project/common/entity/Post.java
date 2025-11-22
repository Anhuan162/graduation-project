package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private boolean isPublished = true;

  private LocalDateTime createdDateTime = LocalDateTime.now();
  private LocalDateTime lastModifiedDateTime = LocalDateTime.now();

  @Builder.Default private PostStatus postStatus = PostStatus.PENDING;

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

//  @ManyToMany
//  @JoinTable(
//      name = "post_tags",
//      joinColumns = @JoinColumn(name = "post_id"),
//      inverseJoinColumns = @JoinColumn(name = "tag_id"))
//  private Set<Tag> tags = new HashSet<>();
}
