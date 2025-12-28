package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility; // Đảm bảo import đúng Enum này
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.mapper.CommentMapper;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.TopicMemberRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final TopicMemberRepository topicMemberRepository;
  private final CurrentUserService currentUserService;
  private final AuthorizationService authorizationService;
  private final FileMetadataRepository fileMetadataRepository;
  private final FileService fileService;

  private final ReactionService reactionService;
  private final CommentMapper commentMapper;

  private static final String CREATED_DATE_TIME_FIELD = "createdDateTime";

  @Transactional
  public CommentResponse createComment(UUID postId, CommentRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canViewTopic(post.getTopic(), currentUser)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Comment rootComment = null;
    User replyToUser = null;

    if (request.getParentId() != null) {
      Comment parent = commentRepository.findById(request.getParentId())
          .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

      if (!parent.getPost().getId().equals(postId)) {
        throw new AppException(ErrorCode.INVALID_PARENT_COMMENT);
      }

      rootComment = parent.getRootComment() != null ? parent.getRootComment() : parent;
      replyToUser = parent.getAuthor();
    }

    Comment comment = Comment.builder()
        .post(post)
        .author(currentUser)
        .rootComment(rootComment)
        .replyToUser(replyToUser)
        .parent(request.getParentId() != null ? commentRepository.getReferenceById(request.getParentId()) : null)
        .content(request.getContent())
        .deleted(false)
        .reactionCount(0L)
        .createdDateTime(Instant.now())
        .build();

    commentRepository.save(comment);

    FileMetadata file = handleFileMetadataReturnEntity(request.getFileMetadataId(), comment, currentUser);
    List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

    CommentPermissionsResponse permissions = CommentPermissionsResponse.builder()
        .canEdit(true)
        .canDelete(true)
        .canReport(false)
        .build();

    return commentMapper.toResponse(
        comment,
        CommentStatsResponse.builder().reactionCount(0L).replyCount(0L).build(),
        CommentUserStateResponse.builder().liked(false).build(),
        permissions,
        attachments);
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getRootComments(UUID postId, Pageable pageable) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(post.getTopic(), currentUser)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Page<Comment> page = commentRepository.findRootComments(postId, pageable);
    return processCommentPage(page, currentUser, post.getTopic(), post.getAuthor().getId());
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getReplies(UUID rootCommentId, Pageable pageable) {
    Comment root = commentRepository.findById(rootCommentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(root.getPost().getTopic(), currentUser)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Page<Comment> page = commentRepository.findAllRepliesByRootId(rootCommentId, pageable);
    return processCommentPage(page, currentUser, root.getPost().getTopic(), root.getPost().getAuthor().getId());
  }

  @Transactional
  public CommentResponse updateComment(UUID commentId, CommentRequest request) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isCommentCreator(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    comment.setContent(request.getContent());
    Comment saved = commentRepository.save(comment);

    FileMetadata file = handleUpdateFile(request.getFileMetadataId(), saved, user);

    boolean isLiked = reactionService.isReactedByMe(saved.getId(), TargetType.COMMENT);
    long replyCount = (saved.getParent() == null) ? commentRepository.countByRootCommentId(saved.getId()) : 0L;
    boolean canModerate = checkModerationRights(user, comment.getPost().getTopic(),
        comment.getPost().getAuthor().getId());

    return buildCommentResponse(saved, user, isLiked, replyCount, false, file, canModerate);
  }

  @Transactional
  public void delete(UUID commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canSoftDeleteComment(comment, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    long childrenCount = commentRepository.countByParentId(commentId);

    if (childrenCount > 0) {
      comment.setDeleted(true);

      fileService.deleteFileByResourceId(comment.getId(), ResourceType.COMMENT);

      commentRepository.save(comment);
      log.info("Soft deleted comment {} because it has replies", commentId);
    } else {

      fileService.deleteFileByResourceId(comment.getId(), ResourceType.COMMENT);

      commentRepository.delete(comment);

      postRepository.decreaseCommentCount(comment.getPost().getId());

      log.info("Hard deleted comment {}", commentId);
    }
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> searchComments(SearchCommentRequest request, Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    boolean isAdmin = authorizationService.isAdmin(user);

    Specification<Comment> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      Join<Comment, Post> postJoin = root.join("post", JoinType.INNER);
      Join<Post, Topic> topicJoin = postJoin.join("topic", JoinType.INNER);

      if (!isAdmin) {
        Predicate isPublic = cb.equal(topicJoin.get("topicVisibility"), TopicVisibility.PUBLIC);

        Subquery<Long> memberSubquery = query.subquery(Long.class);
        Root<TopicMember> memberRoot = memberSubquery.from(TopicMember.class);
        memberSubquery.select(cb.literal(1L));
        memberSubquery.where(
            cb.equal(memberRoot.get("topic"), topicJoin),
            cb.equal(memberRoot.get("user").get("id"), user.getId()),
            cb.isTrue(memberRoot.get("approved")));

        predicates.add(cb.or(isPublic, cb.exists(memberSubquery)));
      }

      if (request.getAuthorId() != null) {
        predicates.add(cb.equal(root.get("author").get("id"), request.getAuthorId()));
      }
      if (request.getPostId() != null) {
        predicates.add(cb.equal(postJoin.get("id"), request.getPostId()));
      }
      if (request.getFromDate() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get(CREATED_DATE_TIME_FIELD),
            request.getFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
      }

      predicates.add(cb.equal(root.get("deleted"), false));

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Comment> page = commentRepository.findAll(spec, pageable);

    return page.map(c -> buildCommentResponse(c, user, false, 0L, false, null, false));
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getMyComments(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Comment> page = commentRepository.findAllByAuthorIdAndDeletedFalse(user.getId(), pageable);

    if (page.isEmpty())
      return Page.empty(pageable);

    List<Comment> comments = page.getContent();
    List<UUID> commentIds = comments.stream().map(Comment::getId).toList();

    Map<UUID, FileMetadata> fileMap = mapSingleFileByCommentId(comments);
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(commentIds, TargetType.COMMENT);
    Map<UUID, Long> replyCountMap = mapReplyCount(commentIds);

    Set<UUID> topicIds = comments.stream()
        .map(c -> c.getPost().getTopic().getId())
        .collect(Collectors.toSet());

    Set<UUID> managedTopicIds = topicIds.isEmpty()
        ? Collections.emptySet()
        : topicMemberRepository.findManagedTopicIds(user.getId(), topicIds, TopicRole.MANAGER);

    boolean isAdmin = authorizationService.isAdmin(user);

    return page.map(c -> {
      boolean isManager = managedTopicIds.contains(c.getPost().getTopic().getId());
      boolean isPostAuthor = c.getPost().getAuthor().getId().equals(user.getId());
      boolean canModeratePost = isAdmin || isManager || isPostAuthor;

      return buildCommentResponse(
          c,
          user,
          likedMap.getOrDefault(c.getId(), false),
          replyCountMap.getOrDefault(c.getId(), 0L),
          false,
          fileMap.get(c.getId()),
          canModeratePost);
    });
  }

  private Page<CommentResponse> processCommentPage(Page<Comment> page, User currentUser, Topic topic,
      UUID postAuthorId) {
    if (page.isEmpty())
      return Page.empty(page.getPageable());

    List<UUID> commentIds = page.getContent().stream().map(Comment::getId).toList();

    Map<UUID, FileMetadata> fileMap = mapSingleFileByCommentId(page.getContent());
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(commentIds, TargetType.COMMENT);
    Map<UUID, Long> replyCountMap = mapReplyCount(commentIds);

    boolean canModeratePost = checkModerationRights(currentUser, topic, postAuthorId);

    return page.map(c -> buildCommentResponse(
        c,
        currentUser,
        likedMap.getOrDefault(c.getId(), false),
        replyCountMap.getOrDefault(c.getId(), 0L),
        false,
        fileMap.get(c.getId()),
        canModeratePost));
  }

  private boolean checkModerationRights(User user, Topic topic, UUID postAuthorId) {
    if (authorizationService.isAdmin(user))
      return true;
    if (authorizationService.isTopicManager(user, topic))
      return true;
    return postAuthorId.equals(user.getId());
  }

  private CommentResponse buildCommentResponse(
      Comment comment,
      User currentUser,
      boolean isLiked,
      Long replyCount,
      boolean isJustCreated,
      FileMetadata file,
      boolean canModeratePost) {

    boolean isCreator = comment.getAuthor().getId().equals(currentUser.getId());
    boolean canDelete = isCreator || canModeratePost;

    CommentPermissionsResponse permissions = CommentPermissionsResponse.builder()
        .canEdit(isCreator)
        .canDelete(canDelete)
        .canReport(!isCreator)
        .build();

    CommentStatsResponse stats = CommentStatsResponse.builder()
        .reactionCount(comment.getReactionCount() != null ? comment.getReactionCount() : 0L)
        .replyCount(replyCount)
        .build();

    CommentUserStateResponse userState = CommentUserStateResponse.builder()
        .liked(isLiked)
        .build();

    List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));

    return commentMapper.toResponse(
        comment,
        stats,
        userState,
        permissions,
        attachments);
  }

  private FileMetadata handleFileMetadataReturnEntity(UUID fileId, Comment comment, User user) {
    if (fileId == null)
      return null;
    FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
        .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

    fileService.updateResourceTarget(comment.getId(), ResourceType.COMMENT, user.getId(), fileMetadata);
    return fileMetadataRepository.save(fileMetadata);
  }

  private FileMetadata handleUpdateFile(UUID newFileId, Comment comment, User user) {
    Optional<FileMetadata> oldFile = fileMetadataRepository
        .findByResourceIdAndResourceType(comment.getId(), ResourceType.COMMENT);

    if (oldFile.isPresent()) {
      FileMetadata f = oldFile.get();
      if (newFileId == null || !f.getId().equals(newFileId)) {
        f.setResourceId(null);
        fileMetadataRepository.save(f);
      }
    }

    return handleFileMetadataReturnEntity(newFileId, comment, user);
  }

  private Map<UUID, Long> mapReplyCount(List<UUID> rootIds) {
    if (rootIds.isEmpty())
      return Collections.emptyMap();
    List<Object[]> rows = commentRepository.countRepliesByRootIds(rootIds);
    return rows.stream().collect(Collectors.toMap(
        row -> (UUID) row[0],
        row -> (Long) row[1]));
  }

  private Map<UUID, FileMetadata> mapSingleFileByCommentId(List<Comment> comments) {
    if (comments.isEmpty())
      return Collections.emptyMap();
    List<UUID> ids = comments.stream().map(Comment::getId).toList();
    List<FileMetadata> files = fileMetadataRepository.findAllByResourceIdInAndResourceType(ids, ResourceType.COMMENT);
    return files.stream()
        .collect(Collectors.toMap(
            FileMetadata::getResourceId,
            Function.identity(),
            (existing, replacement) -> existing));
  }

  private AttachmentResponse toAttachment(FileMetadata f) {
    return AttachmentResponse.builder()
        .id(f.getId().toString())
        .url(f.getUrl())
        .type(f.getContentType())
        .name(f.getFileName())
        .build();
  }
}