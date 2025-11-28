package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "categories")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String name;
  private String description;

  @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  private CategoryType categoryType;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "creator")
  private User creator;

  @JsonIgnore
  @ManyToMany
  @JoinTable(
      name = "category_managers",
      joinColumns = @JoinColumn(name = "category_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Builder.Default
  private Set<User> managers = new HashSet<>();

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Topic> topics = new ArrayList<>();
}
