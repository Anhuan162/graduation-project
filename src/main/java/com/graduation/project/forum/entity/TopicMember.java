package com.graduation.project.forum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topic_members")
public class TopicMember {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private TopicRole topicRole;
  private boolean approved;
  private LocalDateTime joinedAt;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "topic_id")
  private Topic topic;
}
