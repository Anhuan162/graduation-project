package com.graduation.project.forum.service;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.CategoryType;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.TopicMemberRepository;
import com.graduation.project.forum.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

  private final TopicRepository topicRepository;
  private final PostRepository postRepository;
  private final TopicMemberRepository topicMemberRepository;

  public boolean canManageTopic(User currentUser, Topic topic) {
    boolean isAdmin = isAdmin(currentUser);
    boolean isCreator = isTopicCreator(currentUser, topic);
    return isAdmin || isCreator;
  }

  public boolean canCreateTopic(Category category, User user) {
    if (category.getCategoryType() == CategoryType.CLUB
        || category.getCategoryType() == CategoryType.CLASSROOM) {
      return isAdmin(user);
    }
    return true;
  }

  public boolean canCreatePost(Topic topic, User user) {
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      return true;
    }
    return isTopicMember(user, topic);
  }

  public boolean canSoftDeletePost(Post p, User u) {
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
    return isTopicMember(user, topic) || canManageTopic(user, topic);
  }

  public boolean canSoftDeleteComment(Comment comment, User user) {
    return isCommentCreator(comment, user) || canSoftDeletePost(comment.getPost(), user);
  }

  public boolean isTopicCreator(User currentUser, Topic topic) {
    return topic.getCreatedBy().getId().equals(currentUser.getId());
  }

  public boolean isAdmin(User user) {
    return user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
  }

  public boolean isTopicManager(User user, Topic topic) {
    return topicMemberRepository.checkPermission(user.getId(), topic.getId(), TopicRole.MANAGER);
  }

  public boolean isTopicMember(User user, Topic topic) {

    return topicMemberRepository.checkPermission(user.getId(), topic.getId(), null);
  }

  public boolean isCommentCreator(Comment comment, User user) {
    return comment.getAuthor().getId().equals(user.getId());
  }
}
