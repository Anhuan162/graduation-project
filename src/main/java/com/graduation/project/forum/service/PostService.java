package com.graduation.project.forum.service;

import com.github.slugify.Slugify;
import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.constant.TimeRange;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.mapper.PostMapper;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.forum.repository.TopicMemberRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final TopicRepository topicRepository;
  private final TopicMemberRepository topicMemberRepository;
  private final ReactionRepository reactionRepository;
  private final CurrentUserService currentUserService;
  private final PostMapper postMapper;
  private final FileService fileService;
  private final AuthorizationService authorizationService;
  private final FileMetadataRepository fileMetadataRepository;
  private final ApplicationEventPublisher publisher;

  private final Slugify slugify = Slugify.builder().build();

  /**
   * Custom Safelist that allows video tags and their attributes for rich content
   * editing
   */
  private Safelist getCustomSafelist() {
    return Safelist.relaxed()
        .addTags("video", "source")
        .addAttributes("video", "src", "controls", "autoplay", "loop", "muted", "poster", "class", "width",
            "height")
        .addAttributes("source", "src", "type");
  }

  private String generateUniqueSlug(String title) {
    String trimmedTitle = title.trim();
    String baseSlug = slugify.slugify(trimmedTitle);
    if (baseSlug == null || baseSlug.isEmpty()) {
      baseSlug = "post-" + UUID.randomUUID().toString().substring(0, 6);
    }
    String finalSlug = baseSlug;
    int count = 1;

    while (postRepository.existsBySlug(finalSlug)) {
      if (count > 20) {
        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 6);
      }
      finalSlug = baseSlug + "-" + count;
      count++;
    }
    return finalSlug;
  }

  @Transactional
  public PostResponse createPost(UUID topicId, PostRequest request) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    // Auth check moved to Controller @PreAuthorize
    String slug = generateUniqueSlug(request.getTitle());
    String cleanContent = Jsoup.clean(request.getContent(), getCustomSafelist());
    PostStatus status = (request.getStatus() == PostStatus.DRAFT) ? PostStatus.DRAFT : PostStatus.PENDING;

    Post post = Post.builder()
        .title(request.getTitle())
        .content(cleanContent)
        .slug(slug)
        .topic(topic)
        .author(user)
        .postStatus(status)
        .createdDateTime(Instant.now())
        .lastModifiedDateTime(Instant.now())
        .deleted(false)
        .reactionCount(0L)
        .viewCount(0L)
        .build();

    Post saved = postRepository.save(post);

    List<FileMetadata> files = fileService.updateFileMetadataList(
        request.getFileMetadataIds(), saved.getId(), ResourceType.POST);

    if (status != PostStatus.DRAFT) {
      publisher.publishEvent(CreatedPostEvent.from(saved));
    }

    boolean canManage = authorizationService.canManageTopic(user, topic);

    return buildPostResponse(saved, files, user, false, canManage, true);
  }

  @Transactional(readOnly = true)
  public PostResponse getOne(UUID id) {
    Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canViewTopic(post.getTopic(), user)
        && !authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    List<FileMetadata> files = fileMetadataRepository.findByResourceTypeAndResourceIdIn(
        ResourceType.POST, List.of(post.getId()));

    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), post.getId(),
        TargetType.POST);

    boolean canManage = authorizationService.canManageTopic(user, post.getTopic());

    return buildPostResponse(
        post, files, user, isLiked, canManage, authorizationService.isPostCreator(post, user));
  }

  @Transactional
  public PostResponse updateStatus(UUID postId, PostStatus status) {
    return changePostStatus(postId, status);
  }

  @Transactional
  public PostResponse update(UUID id, PostRequest request) {
    Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isPostCreator(post, user))
      throw new AppException(ErrorCode.FORBIDDEN);

    post.setTitle(request.getTitle());
    if (StringUtils.hasText(request.getContent()))
      post.setContent(Jsoup.clean(request.getContent(), getCustomSafelist()));

    if (request.getStatus() != null && request.getStatus() != post.getPostStatus()) {
      if (!post.getPostStatus().canTransitionTo(request.getStatus()))
        throw new AppException(ErrorCode.INVALID_POST_STATUS_TRANSITION);
      if (request.getStatus() == PostStatus.APPROVED && !authorizationService.canManageTopic(user, post.getTopic())) {
        throw new AppException(ErrorCode.FORBIDDEN);
      }
      post.setPostStatus(request.getStatus());
    }
    post.setLastModifiedDateTime(Instant.now());
    Post saved = postRepository.save(post);
    List<FileMetadata> files = fileService.updateFileMetadataList(request.getFileMetadataIds(), saved.getId(),
        ResourceType.POST);
    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), saved.getId(),
        TargetType.POST);
    boolean canManage = authorizationService.canManageTopic(user, saved.getTopic());
    return buildPostResponse(saved, files, user, isLiked, canManage, true);
  }

  @Transactional
  public void delete(UUID id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    postRepository.delete(post);
    postRepository.flush();
  }

  @Transactional
  public PostResponse softDelete(UUID id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canSoftDeletePost(post, user)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    post.setDeleted(true);
    Post saved = postRepository.save(post);

    return buildPostResponse(
        saved,
        Collections.emptyList(),
        user,
        false,
        authorizationService.canManageTopic(user, saved.getTopic()),
        authorizationService.isPostCreator(saved, user));
  }

  @Transactional
  public PostResponse upgradePostStatus(UUID postId, PostStatus postStatus) {
    return changePostStatus(postId, postStatus);
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    boolean isAdmin = authorizationService.isAdmin(user);

    Specification<Post> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      Join<Post, Topic> topicJoin = root.join("topic", JoinType.INNER);

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

      if (StringUtils.hasText(request.getTitle())) {
        String keyword = "%" + request.getTitle().toLowerCase().trim() + "%";

        predicates.add(cb.or(
            cb.like(cb.lower(root.get("title")), keyword),
            cb.like(cb.lower(root.get("content")), keyword)));
      }
      if (request.getPostStatus() != null) {
        predicates.add(cb.equal(root.get("postStatus"), request.getPostStatus()));
      }
      if (StringUtils.hasText(request.getTopicId())) {
        predicates.add(cb.equal(topicJoin.get("id"), UUID.fromString(request.getTopicId())));
      }
      if (request.getAuthorId() != null) {
        predicates.add(cb.equal(root.get("author").get("id"), request.getAuthorId()));
      }

      if (isAdmin && request.getDeleted() != null) {
        predicates.add(cb.equal(root.get("deleted"), request.getDeleted()));
      } else {
        predicates.add(cb.equal(root.get("deleted"), false));
      }

      if (request.getFromDate() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDateTime"), request.getFromDate()));
      }
      if (request.getToDate() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createdDateTime"), request.getToDate()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Post> postPage = postRepository.findAll(spec, pageable);
    return processPostPage(postPage, user);
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> getPostsByUserId(UUID authorId, Pageable pageable) {
    User currentUser = currentUserService.getCurrentUserEntity();
    boolean isOwner = currentUser.getId().equals(authorId);
    boolean isAdmin = authorizationService.isAdmin(currentUser);

    Specification<Post> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("author").get("id"), authorId));
      predicates.add(cb.equal(root.get("deleted"), false));

      if (isOwner || isAdmin) {
      } else {
        predicates.add(cb.equal(root.get("postStatus"), PostStatus.APPROVED));

        Join<Post, Topic> topicJoin = root.join("topic", JoinType.INNER);
        Predicate isPublic = cb.equal(topicJoin.get("topicVisibility"), TopicVisibility.PUBLIC);

        Subquery<Long> memberSubquery = query.subquery(Long.class);
        Root<TopicMember> memberRoot = memberSubquery.from(TopicMember.class);
        memberSubquery.select(cb.literal(1L));
        memberSubquery.where(
            cb.equal(memberRoot.get("topic"), topicJoin),
            cb.equal(memberRoot.get("user").get("id"), currentUser.getId()),
            cb.isTrue(memberRoot.get("approved")));
        predicates.add(cb.or(isPublic, cb.exists(memberSubquery)));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Post> postPage = postRepository.findAll(spec, pageable);
    return processPostPage(postPage, currentUser);
  }

  @Transactional(readOnly = true)
  public Page<DetailPostResponse> getApprovedPostsByTopic(UUID topicId, Pageable pageable) {
    Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(topic, user))
      throw new AppException(ErrorCode.FORBIDDEN);

    Page<Post> postPage = postRepository.findByTopicIdAndPostStatusAndDeletedFalse(topicId, PostStatus.APPROVED,
        pageable);
    if (postPage.isEmpty())
      return Page.empty(pageable);

    Map<UUID, List<String>> urlsByPostId = mapUrls(postPage.getContent());
    boolean canManageTopic = authorizationService.canManageTopic(user, topic);

    return postPage.map(post -> {
      boolean isPostCreator = post.getAuthor().getId().equals(user.getId());
      return DetailPostResponse.from(post, urlsByPostId, canManageTopic, isPostCreator);
    });
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> searchPostsByTopic(UUID topicId, PostStatus postStatus, Pageable pageable) {
    Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(user, topic))
      throw new AppException(ErrorCode.FORBIDDEN);

    Page<Post> postPage = postRepository.findByTopicIdAndPostStatusAndDeletedFalse(topicId, postStatus, pageable);
    return processPostPageInternal(postPage, user, true);
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> getMyPosts(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Post> postPage = postRepository.findAllByAuthor_Id(user.getId(), pageable);
    return processPostPage(postPage, user);
  }

  /**
   * Retrieves featured posts based on the specified time range and optional
   * topic.
   * If no sort is provided in the pageable, defaults to sorting by reaction count
   * descending, then by creation date descending.
   *
   * @param range    the time range for filtering posts (e.g., DAY, WEEK, MONTH,
   *                 ALL)
   * @param topicId  optional topic ID to filter posts by
   * @param pageable pagination and sorting information
   * @return a page of post responses
   */
  @Transactional(readOnly = true)
  public Page<PostResponse> getFeaturedPosts(TimeRange range, UUID topicId, Pageable pageable) {
    if (range == null)
      range = TimeRange.WEEK;

    User user = currentUserService.getCurrentUserEntity();
    boolean isAdmin = authorizationService.isAdmin(user);

    Instant now = Instant.now();
    Instant fromDate = switch (range) {
      case DAY -> now.minus(1, ChronoUnit.DAYS);
      case WEEK -> now.minus(7, ChronoUnit.DAYS);
      case MONTH -> now.minus(30, ChronoUnit.DAYS);
      case ALL -> null;
    };

    Sort effectiveSort = pageable.getSort();
    if (effectiveSort == null || effectiveSort.isUnsorted()) {
      effectiveSort = Sort.by(Sort.Order.desc("reactionCount"), Sort.Order.desc("createdDateTime"));
    }
    Pageable effectivePageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        effectiveSort);

    Specification<Post> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      Join<Post, Topic> topicJoin = root.join("topic", JoinType.INNER);

      // 1) base: approved + not deleted
      predicates.add(cb.equal(root.get("postStatus"), PostStatus.APPROVED));
      predicates.add(cb.equal(root.get("deleted"), false));

      // 2) range filter
      if (fromDate != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDateTime"), fromDate));
      }

      // 3) topic filter (optional)
      if (topicId != null) {
        predicates.add(cb.equal(topicJoin.get("id"), topicId));
      }

      // 4) permission filter (non-admin)
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

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Post> postPage = postRepository.findAll(spec, effectivePageable);
    return processPostPage(postPage, user); // reuse mapper + userState + permissions + attachments
  }

  private PostResponse changePostStatus(UUID postId, PostStatus newStatus) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    if (!post.getPostStatus().canTransitionTo(newStatus)) {
      throw new AppException(ErrorCode.INVALID_POST_STATUS_TRANSITION);
    }
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(user, post.getTopic())) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    post.setPostStatus(newStatus);
    if (newStatus == PostStatus.APPROVED) {
      post.setApprovedBy(user);
      post.setApprovedAt(Instant.now());
    } else {
      post.setApprovedBy(null);
      post.setApprovedAt(null);
    }
    Post saved = postRepository.save(post);
    List<FileMetadata> files = fileMetadataRepository.findByResourceTypeAndResourceIdIn(ResourceType.POST,
        List.of(saved.getId()));
    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), saved.getId(),
        TargetType.POST);
    return buildPostResponse(saved, files, user, isLiked, true, authorizationService.isPostCreator(saved, user));
  }

  private Page<PostResponse> processPostPage(Page<Post> page, User currentUser) {
    return processPostPageInternal(page, currentUser, false);
  }

  private Page<PostResponse> processPostPageInternal(Page<Post> page, User currentUser, boolean forceManager) {
    if (page.isEmpty())
      return Page.empty(page.getPageable());
    List<Post> posts = page.getContent();

    Map<UUID, List<FileMetadata>> filesMap = mapFiles(posts);
    Map<UUID, Boolean> likedMap = mapIsLiked(posts, currentUser);

    Set<UUID> topicIds = posts.stream().map(p -> p.getTopic().getId()).collect(Collectors.toSet());
    Set<UUID> managedTopicIds;

    if (forceManager || authorizationService.isAdmin(currentUser)) {
      managedTopicIds = topicIds;
    } else {
      managedTopicIds = topicMemberRepository.findManagedTopicIds(currentUser.getId(), topicIds, TopicRole.MANAGER);
    }

    return page.map(post -> {
      List<FileMetadata> files = filesMap.getOrDefault(post.getId(), Collections.emptyList());
      boolean canManage = managedTopicIds.contains(post.getTopic().getId()); // O(1) lookup
      boolean isCreator = post.getAuthor().getId().equals(currentUser.getId());

      return buildPostResponse(
          post,
          files,
          currentUser,
          likedMap.getOrDefault(post.getId(), false),
          canManage,
          isCreator);
    });
  }

  private PostResponse buildPostResponse(
      Post post,
      List<FileMetadata> files,
      User currentUser,
      Boolean isLiked,
      boolean canManageTopic,
      boolean isPostCreator) {

    // 1. Build Author Info
    PostAuthorResponse author = null;
    if (post.getAuthor() != null) {
      author = PostAuthorResponse.builder()
          .id(post.getAuthor().getId().toString())
          .fullName(post.getAuthor().getFullName())
          .avatarUrl(post.getAuthor().getAvatarUrl())
          .build();
    }

    // 2. Build Stats
    PostStatsResponse stats = PostStatsResponse.builder()
        .reactionCount(post.getReactionCount() != null ? post.getReactionCount() : 0L)
        .commentCount(post.getCommentCount())
        .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
        .build();

    PostUserStateResponse userState = PostUserStateResponse.builder()
        .liked(Boolean.TRUE.equals(isLiked))
        .build();

    // 4. Build Permissions
    boolean canEdit = isPostCreator || canManageTopic || authorizationService.isAdmin(currentUser);
    boolean canDelete = canEdit;

    PostPermissionsResponse permissions = PostPermissionsResponse.builder()
        .canEdit(canEdit)
        .canDelete(canDelete)
        .canReport(!isPostCreator)
        .build();

    // 5. Build Attachments & Excerpt
    List<String> urls = files == null ? List.of() : files.stream().map(FileMetadata::getUrl).toList();
    List<AttachmentResponse> attachments = toAttachments(files);

    String excerpt = Jsoup.parse(post.getContent()).text();
    excerpt = excerpt.length() > 150 ? excerpt.substring(0, 150) + "..." : excerpt;

    // 6. Map base fields qua Mapper
    PostResponse response = postMapper.toPostResponse(
        post,
        urls,
        excerpt,
        author,
        stats,
        userState,
        permissions,
        attachments);

    if (post.getTopic() != null) {
      var topicInfo = TopicInfoResponse.builder()
          .id(post.getTopic().getId())
          .name(post.getTopic().getTitle())
          .slug(post.getTopic().getSlug())
          .build();

      response.setTopic(topicInfo);

      response.setTopicId(post.getTopic().getId());
    }

    return response;
  }

  private Map<UUID, List<FileMetadata>> mapFiles(List<Post> posts) {
    if (posts == null || posts.isEmpty())
      return Collections.emptyMap();
    List<UUID> ids = posts.stream().map(Post::getId).toList();
    return fileMetadataRepository
        .findByResourceTypeAndResourceIdIn(ResourceType.POST, ids)
        .stream()
        .collect(Collectors.groupingBy(FileMetadata::getResourceId));
  }

  private Map<UUID, List<String>> mapUrls(List<Post> posts) {
    if (posts == null || posts.isEmpty())
      return Collections.emptyMap();
    List<UUID> ids = posts.stream().map(Post::getId).toList();
    List<FileMetadata> all = fileMetadataRepository.findByResourceTypeAndResourceIdIn(ResourceType.POST, ids);
    return all.stream()
        .collect(Collectors.groupingBy(FileMetadata::getResourceId,
            Collectors.mapping(FileMetadata::getUrl, Collectors.toList())));
  }

  private Map<UUID, Boolean> mapIsLiked(List<Post> posts, User user) {
    if (posts == null || posts.isEmpty() || user == null)
      return Collections.emptyMap();
    List<UUID> postIds = posts.stream().map(Post::getId).toList();
    List<UUID> likedIds = reactionRepository.findReactedTargetIdsByUser(user.getId(), TargetType.POST, postIds);
    Set<UUID> likedSet = new HashSet<>(likedIds);
    return postIds.stream().collect(Collectors.toMap(id -> id, likedSet::contains));
  }

  private List<AttachmentResponse> toAttachments(List<FileMetadata> files) {
    if (files == null || files.isEmpty())
      return List.of();
    return files.stream()
        .map(f -> {
          // Extract original filename from Firebase object name
          // (folder/UUID_originalname)
          String originalName = f.getFileName();
          if (originalName != null) {
            // Get the part after the last "/"
            int lastSlashIndex = originalName.lastIndexOf("/");
            if (lastSlashIndex >= 0 && lastSlashIndex < originalName.length() - 1) {
              originalName = originalName.substring(lastSlashIndex + 1);
            }
            // Extract original filename from UUID_originalname
            if (originalName.contains("_")) {
              int underscoreIndex = originalName.indexOf("_");
              if (underscoreIndex > 0 && underscoreIndex < originalName.length() - 1) {
                String prefix = originalName.substring(0, underscoreIndex);
                if (isValidUUID(prefix)) {
                  originalName = originalName.substring(underscoreIndex + 1);
                }
              }
            }
          }
          return AttachmentResponse.builder()
              .id(f.getId().toString())
              .url(f.getUrl())
              .name(originalName)
              .type(f.getContentType())
              .size((long) f.getSize())
              .build();
        })
        .toList();
  }

  /**
   * Checks if the given string is a valid UUID format.
   *
   * @param str the string to check
   * @return true if the string matches UUID regex, false otherwise
   */
  private boolean isValidUUID(String str) {
    if (str == null)
      return false;
    return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  }

}
