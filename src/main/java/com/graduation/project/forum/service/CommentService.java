package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.mapper.CommentMapper;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher publisher;

  // NEW
  private final ReactionService reactionService;
  private final CommentMapper commentMapper;

  private static final String CREATED_DATE_TIME_FIELD = "createdDateTime";

  @Transactional
  public CommentResponse createRootComment(String postId, CommentRequest request) {
    Post post = postRepository
        .findById(UUID.fromString(postId))
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(post.getTopic(), user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Comment comment = buildComment(post, null, user, request.getContent());
    Comment saved = commentRepository.save(comment);

    FileMetadata file = handleFileMetadataReturnEntity(request.getFileMetadataId(), saved, user);
    List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));
    String legacyUrl = file == null ? null : file.getUrl();

    publisher.publishEvent(CreatedCommentEvent.from(saved));

    return buildCommentResponse(
        saved,
        legacyUrl,
        user,
        false,
        authorizationService.canSoftDeletePost(post, user),
        authorizationService.isCommentCreator(saved, user),
        attachments);
  }

  @Transactional
  public CommentResponse replyToComment(String parentId, CommentRequest request) {
    Comment parent = commentRepository
        .findById(UUID.fromString(parentId))
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(parent.getPost().getTopic(), user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Comment reply = buildComment(parent.getPost(), parent, user, request.getContent());
    Comment saved = commentRepository.save(reply);

    FileMetadata file = handleFileMetadataReturnEntity(request.getFileMetadataId(), saved, user);
    List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));
    String legacyUrl = file == null ? null : file.getUrl();

    publisher.publishEvent(CreatedCommentEvent.from(saved));

    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(parent.getPost(), user);

    return buildCommentResponse(
        saved, legacyUrl, user, false, canSoftDeletePost, true, attachments);
  }

  /**
   * Root comments: repo đang trả projection CommentWithReplyCountResponse.
   * Nếu FE muốn unified shape, khuyên đổi repo trả entity + replyCount map.
   * Nhưng để "không phá", ta giữ endpoint cũ.
   *
   * => Khi em nâng tiếp phase 2, anh sẽ hướng dẫn đổi sang trả
   * Page<CommentResponse> unified.
   */
  @Transactional(readOnly = true)
  public Page<CommentWithReplyCountResponse> getRootComments(String postId, Pageable pageable) {
    Post post = postRepository
        .findById(UUID.fromString(postId))
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    Page<CommentWithReplyCountResponse> commentPage = commentRepository
        .findRootCommentsWithCount(UUID.fromString(postId), pageable);

    User currentUser = currentUserService.getCurrentUserEntity();
    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(post, currentUser);

    commentPage.getContent().forEach(c -> {
      boolean isCommentCreator = currentUser.getId().equals(c.getAuthorId());
      c.setCommentCreator(isCommentCreator);
      c.setCanSoftDeletePost(canSoftDeletePost);
    });

    return commentPage;
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getReplies(String commentId, Pageable pageable) {
    UUID parentId = UUID.fromString(commentId);

    Comment parent = commentRepository
        .findById(parentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(parent.getPost().getTopic(), currentUser)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Page<Comment> page = commentRepository.findRepliesByParentId(parentId, pageable);
    if (page.isEmpty())
      return Page.empty(pageable);

    // batch file
    Map<UUID, FileMetadata> fileMap = mapSingleFileByCommentId(page.getContent());

    // batch liked
    List<UUID> ids = page.getContent().stream().map(Comment::getId).toList();
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(ids, TargetType.COMMENT);

    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(parent.getPost(), currentUser);

    return page.map(c -> {
      FileMetadata file = fileMap.get(c.getId());
      String legacyUrl = file == null ? null : file.getUrl();
      List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

      boolean isCreator = authorizationService.isCommentCreator(c, currentUser);

      return buildCommentResponse(
          c,
          legacyUrl,
          currentUser,
          likedMap.getOrDefault(c.getId(), false),
          canSoftDeletePost,
          isCreator,
          attachments);
    });
  }

  @Transactional
  public CommentResponse updateComment(String commentId, CommentRequest request) {
    Comment comment = commentRepository
        .findById(UUID.fromString(commentId))
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isCommentCreator(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    comment.setContent(request.getContent());
    Comment saved = commentRepository.save(comment);

    // Unlink existing FileMetadata if present
    Optional<FileMetadata> existingFile = fileMetadataRepository.findByResourceIdAndResourceType(saved.getId(),
        ResourceType.COMMENT);
    if (existingFile.isPresent()) {
      FileMetadata oldFile = existingFile.get();
      oldFile.setResourceId(null);
      oldFile.setResourceType(null);
      fileMetadataRepository.save(oldFile);
    }

    FileMetadata file = handleFileMetadataReturnEntity(request.getFileMetadataId(), saved, user);
    String legacyUrl = file == null ? null : file.getUrl();
    List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

    boolean isLiked = reactionService.isReactedByMe(saved.getId(), TargetType.COMMENT);
    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(saved.getPost(), user);

    return buildCommentResponse(saved, legacyUrl, user, isLiked, canSoftDeletePost, true, attachments);
  }

  @Transactional
  public CommentResponse softDeleteComment(String commentId) {
    Comment comment = commentRepository
        .findById(UUID.fromString(commentId))
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canSoftDeleteComment(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    comment.setDeleted(true);
    Comment saved = commentRepository.save(comment);

    boolean isCreator = authorizationService.isCommentCreator(saved, user);
    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(saved.getPost(), user);

    return buildCommentResponse(saved, null, user, false, canSoftDeletePost, isCreator, List.of());
  }

  @Transactional
  public Page<CommentResponse> searchComments(SearchCommentRequest request, Pageable pageable) {
    Specification<Comment> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (Objects.nonNull(request.getAuthorId())) {
        predicates.add(cb.equal(root.get("author").get("id"), request.getAuthorId()));
      }
      if (Objects.nonNull(request.getDeleted())) {
        predicates.add(cb.equal(root.get("deleted"), request.getDeleted()));
      }
      if (Objects.nonNull(request.getPostId())) {
        predicates.add(cb.equal(root.get("post").get("id"), request.getPostId()));
      }
      if (request.getFromDate() != null) {
        predicates.add(cb.greaterThanOrEqualTo(
            root.get(CREATED_DATE_TIME_FIELD), request.getFromDate().atStartOfDay()));
      }
      if (request.getToDate() != null) {
        predicates.add(cb.lessThanOrEqualTo(
            root.get(CREATED_DATE_TIME_FIELD), request.getToDate().atTime(23, 59, 59)));
      }

      Objects.requireNonNull(query).orderBy(cb.desc(root.get(CREATED_DATE_TIME_FIELD)));
      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Comment> page = commentRepository.findAll(spec, pageable);
    if (page.isEmpty())
      return Page.empty(pageable);

    User user = currentUserService.getCurrentUserEntity();

    Map<UUID, FileMetadata> fileMap = mapSingleFileByCommentId(page.getContent());
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(
        page.getContent().stream().map(Comment::getId).toList(),
        TargetType.COMMENT);

    return page.map(c -> {
      FileMetadata file = fileMap.get(c.getId());
      String legacyUrl = file == null ? null : file.getUrl();
      List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

      boolean isCreator = authorizationService.isCommentCreator(c, user);
      boolean canSoftDeletePost = authorizationService.canSoftDeletePost(c.getPost(), user);

      return buildCommentResponse(
          c,
          legacyUrl,
          user,
          likedMap.getOrDefault(c.getId(), false),
          canSoftDeletePost,
          isCreator,
          attachments);
    });
  }

  @Transactional
  public DetailCommentResponse getComment(UUID commentId) {
    Comment comment = commentRepository
        .findById(commentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canSoftDeleteComment(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    boolean isCommentCreator = authorizationService.isCommentCreator(comment, user);

    Optional<FileMetadata> fileOpt = fileMetadataRepository.findByResourceIdAndResourceType(comment.getId(),
        ResourceType.COMMENT);
    String url = fileOpt.map(FileMetadata::getUrl).orElse(null);
    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(comment.getPost(), user);

    return DetailCommentResponse.toResponse(comment, url, isCommentCreator, canSoftDeletePost);
  }

  @Transactional
  public Page<CommentResponse> getMyComments(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Comment> page = commentRepository.findAllByAuthorIdAndDeletedFalse(user.getId(), pageable);
    if (page.isEmpty())
      return Page.empty(pageable);

    Map<UUID, FileMetadata> fileMap = mapSingleFileByCommentId(page.getContent());

    List<UUID> ids = page.getContent().stream().map(Comment::getId).toList();
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(ids, TargetType.COMMENT);

    return page.map(c -> {
      FileMetadata file = fileMap.get(c.getId());
      String legacyUrl = file == null ? null : file.getUrl();
      List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

      return buildCommentResponse(
          c, legacyUrl, user, likedMap.getOrDefault(c.getId(), false),
          authorizationService.canSoftDeletePost(c.getPost(), user),
          true,
          attachments);
    });
  }

  // ===================== BUILDERS =====================

  private CommentResponse buildCommentResponse(
      Comment comment,
      String legacyUrl,
      User currentUser,
      Boolean isLiked,
      boolean canSoftDeletePost,
      boolean isCommentCreator,
      List<AttachmentResponse> attachments) {
    // author
    PostAuthorResponse author = null;
    if (comment.getAuthor() != null) {
      author = PostAuthorResponse.builder()
          .id(comment.getAuthor().getId().toString())
          .fullName(comment.getAuthor().getFullName())
          .avatarUrl(comment.getAuthor().getAvatarUrl())
          .build();
    }

    // stats
    CommentStatsResponse stats = CommentStatsResponse.builder()
        .reactionCount(comment.getReactionCount() == null ? 0L : comment.getReactionCount())
        .replyCount(null)
        .build();

    // userState
    CommentUserStateResponse userState = CommentUserStateResponse.builder()
        .isLiked(Boolean.TRUE.equals(isLiked))
        .build();

    // permissions
    boolean canEdit = isCommentCreator;
    boolean canDelete = isCommentCreator || canSoftDeletePost || authorizationService.isAdmin(currentUser);
    boolean canReport = !isCommentCreator;

    CommentPermissionsResponse permissions = CommentPermissionsResponse.builder()
        .canEdit(canEdit)
        .canDelete(canDelete)
        .canReport(canReport)
        .build();

    return commentMapper.toResponse(
        comment,
        legacyUrl,
        author,
        stats,
        userState,
        permissions,
        attachments);
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

  private FileMetadata handleFileMetadataReturnEntity(UUID fileId, Comment comment, User user) {
    if (fileId == null)
      return null;

    FileMetadata fileMetadata = fileMetadataRepository
        .findById(fileId)
        .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

    fileService.updateResourceTarget(comment.getId(), ResourceType.COMMENT, user.getId(), fileMetadata);
    return fileMetadataRepository.save(fileMetadata);
  }

  private Map<UUID, FileMetadata> mapSingleFileByCommentId(List<Comment> comments) {
    if (comments == null || comments.isEmpty())
      return Collections.emptyMap();

    List<UUID> ids = comments.stream().map(Comment::getId).toList();

    return fileMetadataRepository
        .findAllByResourceIdInAndResourceType(ids, ResourceType.COMMENT)
        .stream()
        .collect(Collectors.toMap(FileMetadata::getResourceId, f -> f, (a, b) -> a));
  }

  private AttachmentResponse toAttachment(FileMetadata f) {
    return AttachmentResponse.builder()
        .id(f.getId().toString())
        .url(f.getUrl())
        .name(null)
        .type(null)
        .size(null)
        .build();
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getRootCommentsV2(String postId, Pageable pageable) {
    Post post = postRepository.findById(UUID.fromString(postId))
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(post.getTopic(), currentUser)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Page<Comment> page = commentRepository.findRootCommentsEntity(UUID.fromString(postId), pageable);
    if (page.isEmpty())
      return Page.empty(pageable);

    // batch file
    Map<UUID, FileMetadata> fileMap = mapSingleFileByCommentId(page.getContent());

    // batch isLiked
    List<UUID> ids = page.getContent().stream().map(Comment::getId).toList();
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(ids, TargetType.COMMENT);

    // batch replyCount
    Map<UUID, Long> replyCountMap = mapReplyCount(ids);

    boolean canSoftDeletePost = authorizationService.canSoftDeletePost(post, currentUser);

    return page.map(c -> {
      FileMetadata file = fileMap.get(c.getId());
      String legacyUrl = file == null ? null : file.getUrl();
      List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

      boolean isCreator = authorizationService.isCommentCreator(c, currentUser);

      CommentResponse res = buildCommentResponse(
          c,
          legacyUrl,
          currentUser,
          likedMap.getOrDefault(c.getId(), false),
          canSoftDeletePost,
          isCreator,
          attachments);

      if (res.getStats() != null) {
        res.getStats().setReplyCount(replyCountMap.getOrDefault(c.getId(), 0L));
      }
      return res;
    });
  }

  private Map<UUID, Long> mapReplyCount(List<UUID> parentIds) {
    if (parentIds == null || parentIds.isEmpty())
      return Collections.emptyMap();
    List<Object[]> rows = commentRepository.countRepliesByParentIds(parentIds);

    Map<UUID, Long> map = new HashMap<>();
    for (Object[] r : rows) {
      UUID parentId = (UUID) r[0];
      Long cnt = (Long) r[1];
      map.put(parentId, cnt);
    }
    return map;
  }

}
