package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  @PersistenceContext
  private EntityManager entityManager;

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final TopicMemberRepository topicMemberRepository;
  private final CurrentUserService currentUserService;
  private final AuthorizationService authorizationService;
  private final FileService fileService;

  private final ReactionService reactionService;
  private final CommentMapper commentMapper;

  private static final String CREATED_DATE_TIME_FIELD = "createdDateTime";

  /**
   * Custom Safelist for comment content sanitization
   * Allows basic formatting tags but prevents XSS
   */
  private Safelist getCommentSafelist() {
    return Safelist.basic()
        .addTags("p", "br", "strong", "em", "u", "s", "blockquote", "code", "pre", "img")
        .addAttributes("code", "class")
        .addAttributes("pre", "class")
        .addAttributes("img", "src", "alt", "title", "width", "height");
  }

  // ===============================
  // CREATE COMMENT
  // ===============================
  @Transactional
  public CommentResponse createComment(UUID postId, CommentRequest request) {
    return createCommentLogic(postId, request);
  }

  private CommentResponse createCommentLogic(UUID postId, CommentRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    // Auth check moved to Controller @PreAuthorize

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

    String cleanContent = Jsoup.clean(request.getContent(), getCommentSafelist());

    Comment comment = Comment.builder()
        .post(post)
        .author(currentUser)
        .rootComment(rootComment)
        .replyToUser(replyToUser)
        .parent(request.getParentId() != null ? commentRepository.getReferenceById(request.getParentId()) : null)
        .content(cleanContent)
        .deleted(false)
        .reactionCount(0L)
        .createdDateTime(Instant.now())
        .build();

    commentRepository.save(comment);

    FileMetadata file = fileService.attachFileToResource(request.getFileMetadataId(), comment.getId(),
        ResourceType.COMMENT);

    // Hardcoded isLiked=false and replyCount=0L for performance since this is a
    // newly created comment with no likes or replies yet
    return buildCommentResponse(comment, currentUser, false, 0L, true, file, false);
  }

  // ===============================
  // GET ROOT COMMENTS
  // ===============================
  @Transactional(readOnly = true)
  public Page<CommentResponse> getRootComments(UUID postId, Pageable pageable) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    // Auth check moved to Controller @PreAuthorize

    Page<Comment> page = commentRepository.findRootComments(postId, pageable);
    return processCommentPage(page, currentUser, post.getTopic(), post.getAuthor().getId(), true);
  }

  // ===============================
  // GET REPLIES
  // ===============================
  @Transactional(readOnly = true)
  public Page<CommentResponse> getReplies(UUID rootCommentId, Pageable pageable) {
    Comment root = commentRepository.findById(rootCommentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    // Auth check moved to Controller @PreAuthorize

    Page<Comment> page = commentRepository.findAllRepliesByRootId(rootCommentId, pageable);
    return processCommentPage(page, currentUser, root.getPost().getTopic(), root.getPost().getAuthor().getId(), false);
  }

  // ===============================
  // UPDATE COMMENT (REFACTORED)
  // ===============================
  @Transactional
  public CommentResponse updateComment(UUID commentId, CommentRequest request) {
    // 1. Fetch & Validate Owner
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    // Auth check moved to Controller @PreAuthorize

    // 2. Update Content (Dirty Checking)
    String cleanContent = Jsoup.clean(request.getContent(), getCommentSafelist());
    comment.setContent(cleanContent);
    // Không cần gọi commentRepository.save(comment) nếu đang trong @Transactional
    // và entity đang ở trạng thái Managed. Nhưng gọi explicit cũng không sao.

    // 3. Handle File Attachment (Logic tách riêng, gọn gàng)
    FileMetadata currentFile = fileService.syncFileAttachment(request.getFileMetadataId(), comment.getId(),
        ResourceType.COMMENT);

    // 4. Build Response (Tối ưu: Không query lại những thứ không đổi)
    // Số like và số reply KHÔNG ĐỔI khi edit content -> Lấy từ entity hiện tại hoặc
    // set giá trị cũ
    // Tuy nhiên, vì Comment Entity của cậu không lưu replyCount (field
    // transient/missing),
    // nên ta đành chấp nhận query hoặc trả về 0 nếu frontend tự update state.
    // Tốt nhất: Nên denormalize (lưu replyCount vào bảng Comment).
    // Ở đây tôi giữ query nhưng cảnh báo cậu đây là điểm nghẽn.

    long replyCount = (comment.getRootComment() == null)
        ? commentRepository.countByRootCommentId(comment.getId())
        : 0L; // Hoặc lấy từ cache/filed trong entity nếu có

    boolean isLiked = reactionService.isReactedByMe(comment.getId(), TargetType.COMMENT);

    boolean canModerate = checkModerationRights(currentUser, comment.getPost().getTopic(),
        comment.getPost().getAuthor().getId());

    return buildCommentResponse(comment, currentUser, isLiked, replyCount, false, currentFile, canModerate);
  }

  @Transactional
  public void delete(UUID commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    // Auth check moved to Controller @PreAuthorize

    long childrenCount = commentRepository.countByParentId(commentId);

    fileService.deleteFileByResourceId(comment.getId(), ResourceType.COMMENT);

    if (childrenCount > 0) {
      comment.setDeleted(true);
      comment.setContent(null);

      commentRepository.saveAndFlush(comment);

      postRepository.decreaseCommentCount(comment.getPost().getId());

      log.info("Soft deleted comment {} (hidden content) because it has {} replies", commentId, childrenCount);
    } else {
      postRepository.decreaseCommentCount(comment.getPost().getId());

      commentRepository.delete(comment);

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

      if (request.getAuthorId() != null)
        predicates.add(cb.equal(root.get("author").get("id"), request.getAuthorId()));
      if (request.getPostId() != null)
        predicates.add(cb.equal(postJoin.get("id"), request.getPostId()));
      if (request.getFromDate() != null)
        predicates.add(cb.greaterThanOrEqualTo(
            root.get(CREATED_DATE_TIME_FIELD),
            request.getFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
      predicates.add(cb.equal(root.get("deleted"), false));

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Comment> page = commentRepository.findAll(spec, pageable);
    List<Comment> comments = page.getContent();
    if (comments.isEmpty()) {
      return Page.empty(pageable);
    }
    List<UUID> commentIds = comments.stream().map(Comment::getId).toList();
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(commentIds, TargetType.COMMENT);
    Map<UUID, Long> replyCountMap = mapReplyCount(commentIds);
    Map<UUID, FileMetadata> fileMap = fileService.getFileMapByResourceIds(commentIds, ResourceType.COMMENT);
    return page.map(c -> {
      boolean canModerate = checkModerationRights(user, c.getPost().getTopic(), c.getPost().getAuthor().getId());
      return buildCommentResponse(c, user, likedMap.getOrDefault(c.getId(), false),
          replyCountMap.getOrDefault(c.getId(), 0L), false, fileMap.get(c.getId()), canModerate);
    });
  }

  // ===============================
  // MY COMMENTS
  // ===============================
  @Transactional(readOnly = true)
  public Page<CommentResponse> getMyComments(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Comment> page = commentRepository.findAllByAuthorIdAndDeletedFalse(user.getId(), pageable);

    if (page.isEmpty())
      return Page.empty(pageable);

    List<Comment> comments = page.getContent();
    List<UUID> commentIds = comments.stream().map(Comment::getId).toList();

    Map<UUID, FileMetadata> fileMap = fileService.getFileMapByResourceIds(commentIds, ResourceType.COMMENT);
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(commentIds, TargetType.COMMENT);
    Map<UUID, Long> replyCountMap = mapReplyCount(commentIds);

    Set<UUID> topicIds = comments.stream().map(c -> c.getPost().getTopic().getId()).collect(Collectors.toSet());
    Set<UUID> managedTopicIds = topicIds.isEmpty() ? Collections.emptySet()
        : topicMemberRepository.findManagedTopicIds(user.getId(), topicIds, TopicRole.MANAGER);

    boolean isAdmin = authorizationService.isAdmin(user);

    return page.map(c -> {
      boolean isManager = managedTopicIds.contains(c.getPost().getTopic().getId());
      boolean isPostAuthor = c.getPost().getAuthor().getId().equals(user.getId());
      boolean canModeratePost = isAdmin || isManager || isPostAuthor;

      return buildCommentResponse(c, user, likedMap.getOrDefault(c.getId(), false),
          replyCountMap.getOrDefault(c.getId(), 0L), false, fileMap.get(c.getId()), canModeratePost);
    });
  }

  // ===============================
  // HELPERS
  // ===============================
  private Page<CommentResponse> processCommentPage(Page<Comment> page, User currentUser, Topic topic, UUID postAuthorId,
      boolean isRoot) {
    if (page.isEmpty())
      return Page.empty(page.getPageable());
    List<Comment> comments = page.getContent();
    List<UUID> commentIds = comments.stream().map(Comment::getId).toList();
    Map<UUID, FileMetadata> fileMap = fileService.getFileMapByResourceIds(commentIds, ResourceType.COMMENT);
    Map<UUID, Boolean> likedMap = reactionService.mapIsReactedByMe(commentIds, TargetType.COMMENT);
    Map<UUID, Long> replyCountMap = isRoot ? mapReplyCount(commentIds) : Collections.emptyMap();
    boolean canModeratePost = checkModerationRights(currentUser, topic, postAuthorId);
    return page.map(c -> buildCommentResponse(c, currentUser, likedMap.getOrDefault(c.getId(), false),
        replyCountMap.getOrDefault(c.getId(), 0L), false, fileMap.get(c.getId()), canModeratePost));
  }

  private CommentResponse buildCommentResponse(Comment comment, User currentUser, boolean isLiked, Long replyCount,
      boolean isJustCreated, FileMetadata file, boolean canModeratePost) {
    boolean isCreator = comment.getAuthor() != null && comment.getAuthor().getId().equals(currentUser.getId());
    boolean canDelete = isCreator || canModeratePost;
    CommentPermissionsResponse permissions = CommentPermissionsResponse.builder().canEdit(isCreator)
        .canDelete(canDelete).canReport(!isCreator).build();
    CommentStatsResponse stats = CommentStatsResponse.builder()
        .reactionCount(comment.getReactionCount() != null ? comment.getReactionCount() : 0L)
        .replyCount(replyCount != null ? replyCount : 0L).build();
    CommentUserStateResponse userState = CommentUserStateResponse.builder().liked(isLiked).build();
    List<AttachmentResponse> attachments = file == null ? List.of() : List.of(toAttachment(file));
    return commentMapper.toResponse(comment, stats, userState, permissions, attachments);
  }

  private boolean checkModerationRights(User user, Topic topic, UUID postAuthorId) {
    if (authorizationService.isAdmin(user))
      return true;
    if (authorizationService.isTopicManager(user, topic))
      return true;
    return postAuthorId != null && postAuthorId.equals(user.getId());
  }

  private Map<UUID, Long> mapReplyCount(List<UUID> rootIds) {
    if (rootIds == null || rootIds.isEmpty())
      return Collections.emptyMap();
    List<Object[]> rows = commentRepository.countRepliesByRootIds(rootIds);
    return rows.stream().collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));
  }

  private AttachmentResponse toAttachment(FileMetadata f) {
    return AttachmentResponse.builder().id(f.getId().toString()).url(f.getUrl()).type(f.getContentType())
        .name(f.getFileName()).build();
  }
}
