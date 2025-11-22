package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.Comment;
import com.graduation.project.common.entity.Post;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.CommentRepository;
import com.graduation.project.common.repository.PostRepository;
import com.graduation.project.common.service.AuthorizationService;
import com.graduation.project.user.dto.CommentRequest;
import com.graduation.project.user.dto.CommentResponse;
import com.graduation.project.user.dto.CommentWithReplyCountResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CurrentUserService currentUserService;
  private final AuthorizationService authorizationService;

  @Transactional
  public CommentResponse createRootComment(String postId, CommentRequest request) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    Comment comment =
        Comment.builder()
            .post(post)
            .author(user)
            .content(request.getContent())
            .createdDateTime(LocalDateTime.now())
            .build();

    commentRepository.save(comment);

    return toResponse(comment);
  }

  @Transactional
  public CommentResponse replyToComment(String parentId, CommentRequest request) {

    Comment parent =
        commentRepository
            .findById(UUID.fromString(parentId))
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    Comment reply =
        Comment.builder()
            .parent(parent)
            .post(parent.getPost()) // same post
            .author(user)
            .content(request.getContent())
            .createdDateTime(LocalDateTime.now())
            .build();

    commentRepository.save(reply);

    return toResponse(reply);
  }

  @Transactional(readOnly = true)
  public Page<CommentWithReplyCountResponse> getRootComments(String postId, int page, int size) {

    UUID uuid = UUID.fromString(postId);

    Page<Comment> result =
        commentRepository.findByPostIdAndParentIsNull(uuid, PageRequest.of(page, size));

    return result.map(
        c ->
            CommentWithReplyCountResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorId(c.getAuthor().getId())
                .createdDateTime(c.getCreatedDateTime())
                .repliesCount(commentRepository.countByParentId(c.getId()))
                .build());
  }

  @Transactional
  public CommentResponse updateComment(String commentId, CommentRequest request) {

    Comment comment =
        commentRepository
            .findById(UUID.fromString(commentId))
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!comment.getAuthor().getId().equals(user.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    comment.setContent(request.getContent());
    return toResponse(comment);
  }

  @Transactional
  public void deleteComment(String commentId) {

    Comment comment =
        commentRepository
            .findById(UUID.fromString(commentId))
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    boolean isOwner = comment.getAuthor().getId().equals(user.getId());
    boolean isAdmin = authorizationService.isAdmin(user);

    if (!isOwner && !isAdmin) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // soft delete
    comment.setContent("[deleted]");
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
    UUID id = UUID.fromString(commentId);
    Page<Comment> result = commentRepository.findByParentId(id, pageable);

    return result.map(this::toResponse);
  }

  private CommentResponse toResponse(Comment c) {
    return CommentResponse.builder()
        .id(c.getId())
        .content(c.getContent())
        .authorId(c.getAuthor().getId())
        .createdDateTime(c.getCreatedDateTime())
        .build();
  }
}
