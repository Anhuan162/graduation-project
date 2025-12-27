package com.graduation.project.forum.service;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.CategoryType;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.repository.TopicMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

  private final TopicMemberRepository topicMemberRepository;

  private static final String ROLE_ADMIN = "ADMIN";

  public boolean canManageTopic(User user, Topic topic) {
    if (user == null || topic == null)
      return false;
    return isAdmin(user) || isTopicCreator(user, topic) || isTopicManager(user, topic);
  }

  public boolean canCreateTopic(Category category, User user) {
    if (category == null || user == null)
      return false;

    if (category.getCategoryType() == CategoryType.CLUB
        || category.getCategoryType() == CategoryType.CLASSROOM) {
      return isAdmin(user);
    }
    return true;
  }

  public boolean canViewTopic(Topic topic, User user) {
    if (topic == null)
      return false;

    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC)
      return true;

    if (user == null)
      return false;
    return isAdmin(user) || isTopicMember(user, topic);
  }

  public boolean canCreatePost(Topic topic, User user) {
    if (topic == null || user == null)
      return false;

    if (isAdmin(user))
      return true;

    return isTopicMember(user, topic);
  }

  public boolean canSoftDeletePost(Post post, User user) {
    if (post == null || user == null)
      return false;

    if (isAdmin(user))
      return true;
    if (isPostCreator(post, user))
      return true;

    return canManageTopic(user, post.getTopic());
  }

  public boolean canSoftDeleteComment(Comment comment, User user) {
    if (comment == null || user == null)
      return false;

    if (isAdmin(user))
      return true;
    if (isCommentCreator(comment, user))
      return true;

    Post post = comment.getPost();
    if (post == null)
      return false;

    if (isPostCreator(post, user))
      return true;

    return canManageTopic(user, post.getTopic());
  }

  public boolean isAdmin(User user) {
    if (user == null || user.getRoles() == null)
      return false;
    return user.getRoles().stream()
        .anyMatch(r -> ROLE_ADMIN.equals(r.getName()));
  }

  public boolean isTopicCreator(User user, Topic topic) {
    if (user == null || topic == null)
      return false;
    return topic.getCreatedBy() != null && topic.getCreatedBy().getId().equals(user.getId());
  }

  public boolean isPostCreator(Post post, User user) {
    if (user == null || post == null)
      return false;
    return post.getAuthor() != null && post.getAuthor().getId().equals(user.getId());
  }

  public boolean isCommentCreator(Comment comment, User user) {
    if (user == null || comment == null)
      return false;
    return comment.getAuthor() != null && comment.getAuthor().getId().equals(user.getId());
  }

  public boolean isTopicManager(User user, Topic topic) {
    if (user == null || topic == null)
      return false;
    return topicMemberRepository.existsByUserIdAndTopicIdAndTopicRoleAndApprovedTrue(
        user.getId(),
        topic.getId(),
        TopicRole.MANAGER);
  }

  public boolean isTopicMember(User user, Topic topic) {
    if (user == null || topic == null)
      return false;
    return topicMemberRepository.existsByUserIdAndTopicIdAndApprovedTrue(
        user.getId(),
        topic.getId());
  }
}