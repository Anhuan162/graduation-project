package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationMessageDTO;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.dto.CommentRequest;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.dto.CommentWithReplyCountResponse;
import com.graduation.project.forum.dto.SearchCommentRequest;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CurrentUserService currentUserService;
  private final AuthorizationService authorizationService;
  private final FileMetadataRepository fileMetadataRepository;
  private final FileService fileService;
  private final StreamProducer streamProducer;

  // Constants để tránh hardcode
  private static final String IP_ADDRESS = "127.0.0.1"; // Nên lấy từ Request thực tế
  private static final String NOTI_TITLE_COMMENT = "Bình luận";

  @Transactional
  public CommentResponse createRootComment(String postId, CommentRequest request) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (authorizationService.canViewTopic(post.getTopic(), user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Comment comment = buildComment(post, null, user, request.getContent());
    commentRepository.save(comment);

    String fileUrl = handleFileMetadata(request.getFileMetadataId(), comment, user);

    // Notify Post Author
    sendNotification(
        comment,
        user,
        Set.of(post.getAuthor().getId()),
        NOTI_TITLE_COMMENT,
        user.getFullName() + " đã bình luận vào bài viết của bạn");

    logActivity(
        user,
        "CREATE COMMENT",
        comment.getId(),
        "Bạn đã bình luận vào bài viết của " + post.getAuthor().getFullName());

    return toResponse(comment, fileUrl);
  }

  @Transactional
  public CommentResponse replyToComment(String parentId, CommentRequest request) {
    Comment parent =
        commentRepository
            .findById(UUID.fromString(parentId))
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (authorizationService.canViewTopic(parent.getPost().getTopic(), user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    // Reply vẫn thuộc về Post của parent
    Comment reply = buildComment(parent.getPost(), parent, user, request.getContent());
    commentRepository.save(reply);

    String fileUrl = handleFileMetadata(request.getFileMetadataId(), reply, user);

    // Notify logic (dùng Set để tránh gửi trùng nếu author post và author comment là 1 người)
    Set<UUID> receivers = new HashSet<>();
    if (!user.getId().equals(parent.getPost().getAuthor().getId())) {
      // Notify post owner
      sendNotification(
          reply,
          user,
          Set.of(parent.getPost().getAuthor().getId()),
          NOTI_TITLE_COMMENT,
          user.getFullName() + " đã bình luận vào bài viết của bạn");
    }

    if (!user.getId().equals(parent.getAuthor().getId())) {
      // Notify comment owner
      sendNotification(
          reply,
          user,
          Set.of(parent.getAuthor().getId()),
          NOTI_TITLE_COMMENT,
          user.getFullName() + " đã phản hồi bình luận của bạn");
    }

    return toResponse(reply, fileUrl);
  }

  // --- Optimized Read Methods ---

  @Transactional(readOnly = true)
  public Page<CommentWithReplyCountResponse> getRootComments(String postId, Pageable pageable) {
    // Sử dụng Query tối ưu trong Repository, không loop để count nữa
    return commentRepository.findRootCommentsWithCount(UUID.fromString(postId), pageable);
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
    UUID parentId = UUID.fromString(commentId);
    Page<Comment> comments = commentRepository.findRepliesByParentId(parentId, pageable);

    List<UUID> commentIds = comments.getContent().stream().map(Comment::getId).toList();

    Map<UUID, String> fileMap =
        fileMetadataRepository
            .findAllByResourceIdInAndResourceType(commentIds, ResourceType.COMMENT)
            .stream()
            .collect(
                Collectors.toMap(FileMetadata::getResourceId, FileMetadata::getUrl, (a, b) -> a));

    return comments.map(c -> toResponse(c, fileMap.get(c.getId())));
  }

  private Comment buildComment(Post post, Comment parent, User user, String content) {
    return Comment.builder()
        .post(post)
        .parent(parent)
        .author(user)
        .content(content)
        .createdDateTime(LocalDateTime.now())
        .reactionCount(0L)
        .build();
  }

  private String handleFileMetadata(UUID fileId, Comment comment, User user) {
    if (fileId == null) return null;
    FileMetadata fileMetadata =
        fileMetadataRepository
            .findById(fileId)
            .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

    fileService.updateResourceTarget(
        comment.getId(), ResourceType.COMMENT, user.getId(), fileMetadata);
    fileMetadataRepository.save(fileMetadata);
    return fileMetadata.getUrl();
  }

  private void sendNotification(
      Comment comment, User sender, Set<UUID> receiverIds, String title, String content) {
    if (receiverIds.isEmpty()) return;

    NotificationMessageDTO dto =
        NotificationMessageDTO.builder()
            .relatedId(comment.getId())
            .type(ResourceType.COMMENT)
            .title(title)
            .content(content)
            .senderId(sender.getId())
            .senderName(sender.getEmail()) // Hoặc getFullName
            .receiverIds(receiverIds)
            .createdAt(LocalDateTime.now())
            .build();
    streamProducer.publish(EventEnvelope.from(EventType.NOTIFICATION, dto, "COMMENT"));
  }

  private void logActivity(User user, String action, UUID objectId, String description) {
    ActivityLogDTO log =
        ActivityLogDTO.from(
            user.getId(), action, "FORUM", ResourceType.COMMENT, objectId, description, IP_ADDRESS);
    streamProducer.publish(EventEnvelope.from(EventType.NOTIFICATION, log, "COMMENT"));
  }

  private CommentResponse toResponse(Comment c, String url) {
    return CommentResponse.builder()
        .id(c.getId())
        .content(c.getContent())
        .authorId(c.getAuthor().getId())
        .createdDateTime(c.getCreatedDateTime())
        .url(url)
        .reactionCount(c.getReactionCount())
        .build();
  }

  @Transactional
  public CommentResponse updateComment(String commentId, CommentRequest request) {
    Comment comment =
        commentRepository
            .findById(UUID.fromString(commentId))
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isCommentCreator(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    comment.setContent(request.getContent());
    commentRepository.save(comment);

    String fileUrl = handleFileMetadata(request.getFileMetadataId(), comment, user);

    return toResponse(comment, fileUrl);
  }

  // updateComment và deleteComment giữ nguyên logic nhưng nên dùng helper
  // sendNotification/logActivity
  @Transactional
  public void softDeleteComment(String commentId) {
    Comment comment =
        commentRepository
            .findById(UUID.fromString(commentId))
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canSoftDeleteComment(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    comment.setDeleted(true);
    // Nên xóa luôn liên kết file nếu cần
    commentRepository.save(comment);
  }

  @Transactional
  public Page<CommentResponse> searchComments(SearchCommentRequest request, Pageable pageable) {
    Specification<Comment> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          if (Objects.nonNull(request.getAuthorId())) {
            predicates.add(cb.equal(root.get("user").get("id"), request.getAuthorId()));
          }

          if (Objects.nonNull(request.getIsDeleted())) {
            predicates.add(cb.equal(root.get("isDeleted"), request.getIsDeleted()));
          }

          if (Objects.nonNull(request.getPostId())) {
            predicates.add(cb.equal(root.get("post").get("id"), request.getPostId()));
          }

          if (request.getFromDate() != null) {
            predicates.add(
                cb.greaterThanOrEqualTo(
                    root.get("createdDateTime"), request.getFromDate().atStartOfDay()));
          }
          if (request.getToDate() != null) {
            predicates.add(
                cb.lessThanOrEqualTo(
                    root.get("createdDateTime"), request.getToDate().atTime(23, 59, 59)));
          }

          Objects.requireNonNull(query).orderBy(cb.desc(root.get("createdDateTime")));

          return cb.and(predicates.toArray(new Predicate[0]));
        };
    return commentRepository.findAll(spec, pageable);
  }
}
