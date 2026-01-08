package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.AccessType;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.dto.FileResponse;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.mapper.FileMetadataMapper;
import com.graduation.project.common.service.DriveService;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.constant.SyncStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.*;
import com.graduation.project.common.event.NotificationEvent;
import com.graduation.project.common.event.NotificationType;
import com.graduation.project.forum.dto.PostAcceptedFilterRequest;
import com.graduation.project.forum.dto.PostAcceptedResonse;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Reaction;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.PostMapper;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

  private final PostRepository postRepository;
  private final TopicRepository topicRepository;
  private final CurrentUserService currentUserService;
  private final PostMapper postMapper;
  private final FileService fileService;
  private final AuthorizationService authorizationService;
  private final FileMetadataRepository fileMetadataRepository;
  private final ApplicationEventPublisher publisher;
  private final DriveService driveService;
  private final ReactionRepository reactionRepository;
  private final FileMetadataMapper fileMetadataMapper;
  private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

  @Transactional
  public PostResponse createPost(UUID topicId, PostRequest request) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canCreatePost(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Post post = postMapper.toPost(request, topic, user);
    Post save = postRepository.save(post);

    List<FileMetadata> fileMetadataList = fileService.updateFileMetadataList(
        request.getFileMetadataIds(), save.getId(), ResourceType.POST, user.getId());
    List<FileMetadataResponse> attachments = fileMetadataList.stream().map(fileMetadataMapper::toFileMetadataResponse)
        .toList();

    CreatedPostEvent event = CreatedPostEvent.from(post);
    publisher.publishEvent(event);
    return postMapper.toPostResponse(post, attachments, false);
  }

  @Transactional(readOnly = true)
  public PostResponse getOne(UUID id) {
    Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var userOpt = currentUserService.getCurrentUserEntityOptional();
    User user = userOpt.orElse(null);

    // Якщо bài viết đã Approved -> Ai cũng xem được
    if (PostStatus.APPROVED.equals(post.getPostStatus())) {
      // Valid
    } else {
      // Nếu chưa Approved -> Cần login và có quyền
      if (user == null) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
      if (!authorizationService.canManageTopic(user, post.getTopic())
          && !authorizationService.isPostCreator(post, user)) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
    }

    List<FileMetadataResponse> attachments = getFileMetadataResponses(post.getId());
    Boolean isLiked = (user != null) && checkUserLiked(user.getId(), post.getId());
    return postMapper.toPostResponse(post, attachments, isLiked);
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable) {
    Specification<Post> spec = (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (StringUtils.hasText(request.getTitle())) {
        String searchKey = "%" + request.getTitle().toLowerCase() + "%";
        predicates.add(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchKey));
      }

      if (request.getPostStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("postStatus"), request.getPostStatus()));
      }

      if (StringUtils.hasText(request.getTopicId())) {
        predicates.add(criteriaBuilder.equal(root.get("topicId"), request.getTopicId()));
      }

      if (request.getAuthorId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("authorId"), request.getAuthorId()));
      }

      predicates.add(criteriaBuilder.equal(root.get("deleted"), request.getDeleted()));

      if (request.getFromDate() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("createdDateTime"), request.getFromDate()));
      }
      if (request.getToDate() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(
                root.get("createdDateTime"), request.getToDate()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };

    Page<Post> posts = postRepository.findAll(spec, pageable);
    User currentUser = currentUserService.getCurrentUserEntity();
    Map<UUID, List<FileMetadataResponse>> filesMap = mapPostWithFileMetadata(posts);
    Map<UUID, Boolean> likedMap = mapPostWithUserLiked(posts, currentUser.getId());

    return posts.map(
        post -> postMapper.toPostResponse(
            post,
            filesMap.getOrDefault(post.getId(), List.of()),
            likedMap.getOrDefault(post.getId(), false)));
  }

  @Transactional(readOnly = true)
  public Page<DetailPostResponse> getPostsByTopic(UUID topicId, Pageable pageable) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Check if user can manage the topic (creator or manager)
    boolean isManager = authorizationService.canManageTopic(user, topic);

    Page<Post> postPage;

    if (isManager) {
      // Managers see ALL posts (APPROVED, PENDING, REJECTED)
      postPage = postRepository.findByTopicIdAndDeletedFalse(topicId, pageable);
    } else {
      // Regular users see:
      // 1. All APPROVED posts
      // 2. Their own PENDING/REJECTED posts (Ghost Pattern)
      postPage = postRepository.findPublicAndOwnPosts(topicId, user.getId(), pageable);
    }

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }
    Map<UUID, List<FileMetadataResponse>> filesMap = mapPostWithFileMetadata(postPage);
    Map<UUID, Boolean> likedMap = user != null ? mapPostWithUserLiked(postPage, user.getId()) : Collections.emptyMap();

    boolean canManageTopic = authorizationService.canManageTopic(user, topic);

    return postPage.map(
        post -> {
          boolean isPostCreator = authorizationService.isPostCreator(post, user);
          return DetailPostResponse.from(
              post,
              filesMap,
              canManageTopic,
              isPostCreator,
              likedMap.getOrDefault(post.getId(), false));
        });
  }

  @Transactional
  public PostResponse update(UUID id, PostRequest request) {
    Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Validate post status - only allow editing PENDING posts
    if (!PostStatus.PENDING.equals(post.getPostStatus())) {
      throw new AppException(ErrorCode.CANNOT_EDIT_APPROVED_POST);
    }

    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    postRepository.save(post);

    // Filter and delete removed files
    List<FileMetadata> currentFiles = fileService.findFileMetadataByResourceTarget(post.getId(), ResourceType.POST);
    Set<UUID> newFileIds = request.getFileMetadataIds() == null
        ? new HashSet<>()
        : new HashSet<>(request.getFileMetadataIds());

    for (FileMetadata file : currentFiles) {
      if (!newFileIds.contains(file.getId())) {
        if (file.getUser().getId().equals(user.getId())) {
          fileService.deleteFile(file.getId());
        }
      }
    }

    List<FileMetadata> fileMetadataList = fileService.updateFileMetadataList(
        request.getFileMetadataIds(), post.getId(), ResourceType.POST, user.getId());
    List<FileMetadataResponse> attachments = fileMetadataList.stream().map(fileMetadataMapper::toFileMetadataResponse)
        .toList();
    Boolean isLiked = checkUserLiked(user.getId(), post.getId());
    return postMapper.toPostResponse(post, attachments, isLiked);
  }

  public void delete(String id) {
    Post post = postRepository
        .findById(UUID.fromString(id))
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    postRepository.delete(post);
  }

  public PostResponse softDelete(String id) {
    Post post = postRepository
        .findById(UUID.fromString(id))
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canSoftDeletePost(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    post.setDeleted(true);

    postRepository.save(post);
    return postMapper.toPostResponse(post, Collections.emptyList(), false);
  }

  @Transactional
  public PostResponse upgradePostStatus(UUID postId, PostStatus postStatus) {
    Post post = postRepository
        .findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(currentUser, post.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    post.setPostStatus(postStatus);
    post.setApprovedBy(currentUser);
    post.setApprovedAt(LocalDateTime.now());

    postRepository.save(post);

    List<FileMetadataResponse> attachments = getFileMetadataResponses(post.getId());
    Boolean isLiked = checkUserLiked(currentUser.getId(), post.getId());

    if (PostStatus.APPROVED.equals(postStatus)) {
      log.info("Publishing POST_APPROVED notification for post {} to user {}", post.getId(), post.getAuthor().getId());
      publisher.publishEvent(new NotificationEvent(
          post.getAuthor().getId().toString(),
          "SYSTEM",
          "Bài viết của bạn đã được duyệt",
          "Bài viết \"" + post.getTitle() + "\" đã hiển thị công khai.",
          NotificationType.POST_APPROVED,
          post.getId().toString(),
          "POST"));
    } else if (PostStatus.REJECTED.equals(postStatus)) {
      log.info("Publishing POST_REJECTED notification for post {} to user {}", post.getId(), post.getAuthor().getId());
      publisher.publishEvent(new NotificationEvent(
          post.getAuthor().getId().toString(),
          "SYSTEM",
          "Bài viết bị từ chối",
          "Bài viết \"" + post.getTitle() + "\" đã bị từ chối phê duyệt. Bạn có thể chỉnh sửa và gửi lại.",
          NotificationType.POST_REJECTED,
          post.getId().toString(),
          "POST"));
    }

    return postMapper.toPostResponse(post, attachments, isLiked);
  }

  @Transactional
  public Page<PostResponse> searchPostsByTopic(
      UUID topicId, PostStatus postStatus, Pageable pageable) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    return postRepository
        .findByTopicIdAndPostStatusAndDeletedFalse(topicId, postStatus, pageable)
        .map(post -> postMapper.toPostResponse(post, List.of(), false));
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> getPostsByUserId(UUID userId, Pageable pageable) {
    var userOpt = currentUserService.getCurrentUserEntityOptional();
    User currentUser = userOpt.orElse(null);
    boolean isOwnerOrAdmin = currentUser != null
        && (currentUser.getId().equals(userId) || authorizationService.isAdmin(currentUser));

    Page<Post> postPage;
    if (isOwnerOrAdmin) {
      postPage = postRepository.findAllByAuthor_Id(userId, pageable);
    } else {
      postPage = postRepository.findAllByAuthor_IdAndPostStatus(userId, PostStatus.APPROVED, pageable);
    }

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Map<UUID, List<FileMetadataResponse>> filesMap = mapPostWithFileMetadata(postPage);
    Map<UUID, Boolean> likedMap = currentUser != null ? mapPostWithUserLiked(postPage, currentUser.getId())
        : Collections.emptyMap();
    return postPage.map(
        post -> postMapper.toPostResponse(
            post,
            filesMap.getOrDefault(post.getId(), List.of()),
            likedMap.getOrDefault(post.getId(), false)));
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> getMyPosts(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Post> postPage = postRepository.findAllByAuthor_Id(user.getId(), pageable);

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Map<UUID, List<FileMetadataResponse>> filesMap = mapPostWithFileMetadata(postPage);
    Map<UUID, Boolean> likedMap = mapPostWithUserLiked(postPage, user.getId());
    return postPage.map(
        post -> postMapper.toPostResponse(
            post,
            filesMap.getOrDefault(post.getId(), List.of()),
            likedMap.getOrDefault(post.getId(), false)));
  }

  private Map<UUID, List<FileMetadataResponse>> mapPostWithFileMetadata(Page<Post> postPage) {
    List<UUID> postIds = postPage.getContent().stream().map(Post::getId).toList();

    List<FileMetadata> allFiles = fileMetadataRepository.findByResourceTypeAndResourceIdIn(ResourceType.POST, postIds);
    return allFiles.stream()
        .collect(
            Collectors.groupingBy(
                FileMetadata::getResourceId,
                Collectors.mapping(
                    fileMetadataMapper::toFileMetadataResponse, Collectors.toList())));
  }

  private Map<UUID, Boolean> mapPostWithUserLiked(Page<Post> postPage, UUID userId) {
    List<UUID> postIds = postPage.getContent().stream().map(Post::getId).toList();
    List<Reaction> reactions = reactionRepository.findByUser_IdAndTargetTypeAndTargetIdIn(
        userId, TargetType.POST, postIds);
    return reactions.stream().collect(Collectors.toMap(Reaction::getTargetId, r -> true));
  }

  private List<FileMetadataResponse> getFileMetadataResponses(UUID postId) {
    List<String> urls = fileService.getFileMetadataIds(postId, ResourceType.POST);
    List<FileMetadata> files = fileMetadataRepository.findByResourceTypeAndResourceIdIn(
        ResourceType.POST, List.of(postId));
    return files.stream().map(fileMetadataMapper::toFileMetadataResponse).toList();
  }

  private Boolean checkUserLiked(UUID userId, UUID postId) {
    return reactionRepository
        .findByUser_IdAndTargetIdAndTargetType(userId, postId, TargetType.POST)
        .isPresent();
  }

  public List<PostAcceptedResonse> searchPostAccepted(
      PostAcceptedFilterRequest postAcceptedRequest) {
    // Step 1: Query Posts (basic filtering at DB level)
    List<Post> posts = postRepository.getPostAccepted(postAcceptedRequest);

    if (posts.isEmpty()) {
      return List.of();
    }

    // Step 2: Extract Post IDs
    List<UUID> postIds = posts.stream().map(Post::getId).toList();

    // Step 3: Bulk fetch FileMetadata (1 query for all posts)
    List<FileMetadata> metadataList = fileMetadataRepository.findByResourceTypeAndResourceIdIn(ResourceType.POST,
        postIds);

    // Step 4: Convert to Map for O(1) lookup
    Map<UUID, FileMetadata> metadataMap = metadataList.stream()
        .collect(
            Collectors.toMap(
                FileMetadata::getResourceId, fm -> fm, (existing, replacement) -> existing));

    // Step 5: Map to DTO with SyncStatus calculation
    List<PostAcceptedResonse> result = posts.stream()
        .map(
            post -> {
              FileMetadata fm = metadataMap.get(post.getId());
              SyncStatus status = calculateSyncStatus(post, fm);

              PostAcceptedResonse response = post.toPostAcceptedResonse();
              response.setSyncStatus(status);
              return response;
            })
        .toList();

    // Step 6: Filter by syncStatus if specified
    if (postAcceptedRequest.getSyncStatus() != null) {
      return result.stream()
          .filter(p -> p.getSyncStatus() == postAcceptedRequest.getSyncStatus())
          .toList();
    }

    return result;
  }

  private SyncStatus calculateSyncStatus(Post post, FileMetadata fileMetadata) {
    if (fileMetadata == null) {
      return SyncStatus.NOT_SYNCED;
    }

    LocalDateTime syncedAt = fileMetadata.getCreatedAt();
    if (syncedAt == null) {
      return SyncStatus.SYNCED; // Fallback if createdAt is missing
    }

    // Check if post was modified after the file was created on Drive
    if (post.getLastModifiedDateTime() != null
        && post.getLastModifiedDateTime().isAfter(syncedAt)) {
      return SyncStatus.OUTDATED;
    }

    // CRITICAL: Check if any comments were added/modified after sync
    // Since comments are part of the exported content, new comments = outdated
    // content
    if (post.getComments() != null && !post.getComments().isEmpty()) {
      LocalDateTime latestCommentTime = post.getComments().stream()
          .map(Comment::getCreatedDateTime)
          .filter(dt -> dt != null)
          .max(LocalDateTime::compareTo)
          .orElse(null);

      if (latestCommentTime != null && latestCommentTime.isAfter(syncedAt)) {
        return SyncStatus.OUTDATED;
      }
    }

    return SyncStatus.SYNCED;
  }

  @Async
  // ❌ REMOVED @Transactional here to avoid locking DB during upload
  public void upLoadPostAndCommentToDrive(PostAcceptedSelectList postAcceptedSelectList, String requesterUserId) {
    try {
      log.info(
          "Starting background upload for {} posts...", postAcceptedSelectList.getPostIds().size());

      // =================================================================
      // PHASE 1: FETCH DATA (Read-Only Transaction)
      // =================================================================
      // Purpose: Fetch posts and eagerly load comments before session closes
      List<Post> posts = transactionTemplate.execute(status -> {
        List<Post> fetchedPosts = postRepository.findAllById(postAcceptedSelectList.getPostIds());

        // ⚠️ CRITICAL: Eagerly initialize lazy-loaded comments to avoid
        // LazyInitializationException
        // Once we exit this block, Hibernate session closes, so we must trigger loading
        // now
        fetchedPosts.forEach(post -> {
          if (post.getComments() != null) {
            int commentCount = post.getComments().size(); // Force Hibernate to load comments
            log.debug("[PHASE 1] Post ID: {} - Loaded {} comments", post.getId(), commentCount);
          } else {
            log.debug("[PHASE 1] Post ID: {} - No comments collection", post.getId());
          }
        });
        log.info("[PHASE 1] Successfully loaded {} posts with comments", fetchedPosts.size());
        return fetchedPosts;
      });

      if (posts == null || posts.isEmpty()) {
        log.warn("No posts found for upload");
        return;
      }

      // =================================================================
      // PHASE 2: BUILD HTML (No Transaction - Pure Computation)
      // =================================================================
      StringBuilder htmlBuilder = new StringBuilder();
      htmlBuilder.append("<html><body>");

      for (Post post : posts) {
        // Post Title & Content
        htmlBuilder.append("<h1>").append(post.getTitle()).append("</h1>");
        htmlBuilder.append("<div>").append(post.getContent()).append("</div>");

        // Comments
        htmlBuilder.append("<h3>Comments:</h3><ul>");
        if (post.getComments() != null) {
          int commentCount = post.getComments().size();
          log.debug("[PHASE 2] Rendering {} comments for Post ID: {}", commentCount, post.getId());
          for (Comment comment : post.getComments()) {
            htmlBuilder.append("<li>").append(comment.getContent()).append("</li>");
          }
        } else {
          log.warn("[PHASE 2] Comments collection is NULL for Post ID: {} - LazyInit error likely!", post.getId());
        }
        htmlBuilder.append("</ul><hr/>");
      }
      htmlBuilder.append("</body></html>");

      // Convert to Stream
      byte[] contentBytes = htmlBuilder.toString().getBytes(StandardCharsets.UTF_8);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

      // =================================================================
      // PHASE 3: NETWORK I/O (No Transaction - Slow Operation)
      // =================================================================
      // 🚀 Upload to Google Drive (may take several seconds)
      // Database connection is NOT held during this time - SAFE!
      FileResponse response = driveService.uploadFile(inputStream, postAcceptedSelectList.getNameFile(), "text/html");

      log.info("Upload successful! File ID: {}", response.getFileId());

      // =================================================================
      // PHASE 4: SAVE METADATA (Write Transaction - Short & Fast)
      // =================================================================
      // Open a new, short transaction just to save results
      transactionTemplate.executeWithoutResult(status -> {
        for (Post post : posts) {
          FileMetadata fileMetadata = FileMetadata.builder()
              .fileName(postAcceptedSelectList.getNameFile())
              .url(response.getFileId()) // Store Drive file ID
              .contentType("application/vnd.google-apps.document") // Google Docs MIME type
              .size(contentBytes.length)
              .accessType(AccessType.PRIVATE) // System upload, not public
              .resourceType(ResourceType.POST)
              .resourceId(post.getId())
              .onDrive(true)
              .createdAt(LocalDateTime.now())
              .user(null) // System upload, no specific user
              .build();

          fileMetadataRepository.save(fileMetadata);
          log.info("FileMetadata saved for post: {}", post.getId());
        }
      });

      // =================================================================
      // PHASE 5: NOTIFICATION (No Transaction)
      // =================================================================
      publisher.publishEvent(new NotificationEvent(
          requesterUserId, // Người nhận (Người bấm nút Export)
          "SYSTEM", // Sender
          "Export Drive thành công",
          "File " + postAcceptedSelectList.getNameFile() + " đã sẵn sàng trên Drive.",
          NotificationType.DRIVE_UPLOAD,
          response.getFileId(), // Reference ID là File ID trên Drive
          "DOCUMENT"));

    } catch (Exception e) {
      log.error("Failed to upload posts to Drive", e);
      // ⚠️ FIRE EVENT FAIL
      publisher.publishEvent(new NotificationEvent(
          requesterUserId,
          "SYSTEM",
          "Export Drive thất bại",
          "Đã có lỗi xảy ra khi lưu file " + postAcceptedSelectList.getNameFile(),
          NotificationType.DRIVE_UPLOAD, // Keep same type or use generic
          "",
          "ERROR"));
      // TODO: Update DB status to ERROR
    }
  }

  public List<PostResponse> getTopReactedPosts() {
    return postRepository.findTop10ByOrderByReactionCountDesc().stream()
        .map(post -> postMapper.toPostResponse(post, List.of(), false))
        .toList();
  }

  public List<PostStatDTO> getDailyPostStats() {
    return postRepository.countPostsByDate(java.time.LocalDateTime.now().minusDays(30));
  }

  public List<PostStatDTO> getMonthlyPostStats() {
    return postRepository.countPostsByMonth(java.time.LocalDateTime.now().minusMonths(12));
  }
}
