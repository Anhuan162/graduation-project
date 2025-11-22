package com.graduation.project.common.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

  private final CurrentUserService currentUserService;

  public boolean canNotManageTopic(User currentUser, Topic topic) {
    boolean isAdmin = isAdmin(currentUser);
    boolean isCreator = isTopicCreator(currentUser, topic);
    return !isAdmin && !isCreator;
  }

  public void checkCanCreateTopic(Category category, User user) {
    if (category.getCategoryType() == CategoryType.CLUB
        || category.getCategoryType() == CategoryType.CLASSROOM) {
      if (!isAdmin(user)) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
    }
  }

  public boolean canDeletePost(Post p, User u) {
    if (isAdmin(u)) return true;
    if (isPostCreator(p, u)) return true;
    if (isTopicCreator(u, p.getTopic())) return true;
    return isTopicManager(u, p.getTopic());
  }

  public boolean isPostCreator(Post post, User user) {
    return post.getAuthor().getId().equals(user.getId());
  }

  public boolean canViewTopic(Topic topic, User user) {
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) return true;
    return topic.getTopicMembers().stream()
        .anyMatch(m -> m.getUser().getId().equals(user.getId()) && m.isApproved());
  }

  private static boolean isTopicCreator(User currentUser, Topic topic) {
    return topic.getCreatedBy().getId().equals(currentUser.getId());
  }

  public boolean isAdmin(User user) {
    return user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
  }

  private boolean isTopicManager(User user, Topic topic) {
    return topic.getTopicMembers().stream()
        .anyMatch(
            m ->
                m.getUser().getId().equals(user.getId())
                    && m.isApproved()
                    && m.getTopicRole() == TopicRole.MANAGER);
  }
}
