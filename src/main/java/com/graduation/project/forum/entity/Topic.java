package com.graduation.project.forum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topics")
public class Topic {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  private String title;
  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime lastModifiedAt = LocalDateTime.now();

  @Column(columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  private TopicVisibility topicVisibility = TopicVisibility.PUBLIC;

  @Builder.Default
  private boolean isDeleted = Boolean.FALSE;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "createdBy")
  private User createdBy;

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<TopicMember> topicMembers = new HashSet<>();

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Post> posts = new ArrayList<>();
}
