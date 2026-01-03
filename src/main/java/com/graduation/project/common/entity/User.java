package com.graduation.project.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.project.auth.dto.response.UserProfileResponse;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.cpa.entity.CpaProfile;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String email;

  private String password;

  private String fullName;

  private Boolean enabled;

  private String avatarUrl;

  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  private String studentCode;

  private String classCode;

  private LocalDateTime registrationDate;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<FileMetadata> fileMetadata = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private CpaProfile cpaProfile;

  @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Category> categories = new ArrayList<>();

  @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Topic> topics = new ArrayList<>();

  @ManyToMany(mappedBy = "managers")
  @JsonIgnore
  @Builder.Default
  private Set<Category> managedCategories = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<TopicMember> topicMembers = new HashSet<>();

  public UserProfileResponse toUserProfileResponse() {
    return UserProfileResponse.builder()
        .email(this.email)
        .fullName(this.fullName)
        .phone(this.phone)
        .avatarUrl(this.avatarUrl)
        .studentCode(this.studentCode)
        .classCode(this.classCode)
        .build();
  }
}
