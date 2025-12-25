package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.PostMapper;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final TopicRepository topicRepository;
  private final ReactionRepository reactionRepository;
  private final CurrentUserService currentUserService;
  private final PostMapper postMapper;
  private final FileService fileService;
  private final AuthorizationService authorizationService;
  private final FileMetadataRepository fileMetadataRepository;
  private final ApplicationEventPublisher publisher;

  // ===================== CREATE =====================
  @Transactional
  public PostResponse createPost(UUID topicId, PostRequest request) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canCreatePost(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Post post = postMapper.toPost(request, topic, user);
    Post saved = postRepository.save(post);

    List<FileMetadata> files = fileService.updateFileMetadataList(
        request.getFileMetadataIds(), saved.getId(), ResourceType.POST, user.getId());

    publisher.publishEvent(CreatedPostEvent.from(saved));

    return buildPostResponse(
        saved,
        files,
        user,
        false,
        authorizationService.canManageTopic(user, topic),
        true);
  }

  // ===================== READ ONE =====================
  @Transactional
  public PostResponse getOne(UUID id) {
    Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canViewTopic(post.getTopic(), user)
        && !authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    List<FileMetadata> files = fileMetadataRepository.findByResourceTypeAndResourceIdIn(
        ResourceType.POST, List.of(post.getId()));

    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), post.getId(),
        TargetType.POST);

    return buildPostResponse(
        post,
        files,
        user,
        isLiked,
        authorizationService.canManageTopic(user, post.getTopic()),
        authorizationService.isPostCreator(post, user));
  }

  // ===================== UPDATE =====================
  @Transactional
  public PostResponse updateStatus(UUID postId, PostStatus status) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    post.setPostStatus(status);
    User holUser = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(holUser, post.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    if (status == PostStatus.APPROVED) {
      post.setApprovedAt(LocalDateTime.now());
      post.setApprovedBy(holUser);
    }

    postRepository.save(post);
    List<FileMetadata> files = fileMetadataRepository
        .findByResourceTypeAndResourceIdIn(ResourceType.POST, List.of(post.getId()));

    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(
        holUser.getId(), post.getId(), TargetType.POST);

    return buildPostResponse(
        post,
        files,
        holUser,
        isLiked,
        authorizationService.canManageTopic(holUser, post.getTopic()),
        authorizationService.isPostCreator(post, holUser));

  }

  @Transactional
  public PostResponse update(UUID id, PostRequest request) {
    Post post = postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    Post saved = postRepository.save(post);

    List<FileMetadata> files = fileService.updateFileMetadataList(
        request.getFileMetadataIds(), saved.getId(), ResourceType.POST, user.getId());

    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), saved.getId(),
        TargetType.POST);

    return buildPostResponse(
        saved,
        files,
        user,
        isLiked,
        authorizationService.canManageTopic(user, saved.getTopic()),
        true);
  }

  // ===================== DELETE (HARD) =====================
  @Transactional
  public void delete(String id) {
    Post post = postRepository
        .findById(UUID.fromString(id))
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    // giữ đúng logic cũ: chỉ ADMIN được hard delete
    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    postRepository.delete(post);
  }

  // ===================== SOFT DELETE =====================
  @Transactional
  public PostResponse softDelete(UUID id) {
    Post post = postRepository
        .findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canSoftDeletePost(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    post.setDeleted(true);
    Post saved = postRepository.save(post);

    // soft delete không cần files
    return buildPostResponse(
        saved,
        Collections.emptyList(),
        user,
        false,
        authorizationService.canManageTopic(user, saved.getTopic()),
        authorizationService.isPostCreator(saved, user));
  }

  // ===================== SEARCH (ADMIN) =====================
  @Transactional
  public Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Specification<Post> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (StringUtils.hasText(request.getTitle())) {
        predicates.add(
            cb.like(cb.lower(root.get("title")), "%" + request.getTitle().toLowerCase() + "%"));
      }

      if (request.getPostStatus() != null) {
        predicates.add(cb.equal(root.get("postStatus"), request.getPostStatus()));
      }

      if (StringUtils.hasText(request.getTopicId())) {
        predicates.add(cb.equal(root.get("topic").get("id"), UUID.fromString(request.getTopicId())));
      }

      if (request.getAuthorId() != null) {
        predicates.add(cb.equal(root.get("author").get("id"), request.getAuthorId()));
      }

      predicates.add(cb.equal(root.get("deleted"), Boolean.TRUE.equals(request.getDeleted())));

      if (request.getFromDate() != null) {
        predicates.add(
            cb.greaterThanOrEqualTo(root.get("createdDateTime"), request.getFromDate()));
      }
      if (request.getToDate() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createdDateTime"), request.getToDate()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Post> postPage = postRepository.findAll(spec, pageable);
    if (postPage.isEmpty())
      return Page.empty(pageable);

    Map<UUID, List<FileMetadata>> filesMap = mapFiles(postPage.getContent());
    Map<UUID, Boolean> likedMap = mapIsLiked(postPage.getContent(), user);

    return postPage.map(
        post -> {
          List<FileMetadata> files = filesMap.getOrDefault(post.getId(), Collections.emptyList());
          return buildPostResponse(
              post,
              files,
              user,
              likedMap.getOrDefault(post.getId(), false),
              authorizationService.canManageTopic(user, post.getTopic()),
              authorizationService.isPostCreator(post, user));
        });
  }

  // ===================== APPROVED POSTS BY TOPIC (PUBLIC VIEW OF TOPIC)
  // =====================
  @Transactional
  public Page<DetailPostResponse> getApprovedPostsByTopic(UUID topicId, Pageable pageable) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Page<Post> postPage = postRepository.findByTopicIdAndPostStatusAndDeletedFalse(topicId, PostStatus.APPROVED,
        pageable);

    if (postPage.isEmpty())
      return Page.empty(pageable);

    Map<UUID, List<String>> urlsByPostId = mapUrls(postPage.getContent());

    boolean canManageTopic = authorizationService.canManageTopic(user, topic);

    return postPage.map(
        post -> {
          boolean isPostCreator = authorizationService.isPostCreator(post, user);
          return DetailPostResponse.from(post, urlsByPostId, canManageTopic, isPostCreator);
        });
  }

  // ===================== UPGRADE STATUS =====================
  @Transactional
  public PostResponse upgradePostStatus(UUID postId, PostStatus postStatus) {
    Post post = postRepository
        .findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, post.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    post.setPostStatus(postStatus);
    post.setApprovedBy(user);
    post.setApprovedAt(LocalDateTime.now());
    Post saved = postRepository.save(post);

    List<FileMetadata> files = fileMetadataRepository.findByResourceTypeAndResourceIdIn(
        ResourceType.POST, List.of(saved.getId()));

    boolean isLiked = reactionRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), saved.getId(),
        TargetType.POST);

    return buildPostResponse(
        saved,
        files,
        user,
        isLiked,
        true,
        authorizationService.isPostCreator(saved, user));
  }

  // ===================== SEARCH POSTS BY TOPIC (MANAGER) =====================
  @Transactional
  public Page<PostResponse> searchPostsByTopic(UUID topicId, PostStatus postStatus, Pageable pageable) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Page<Post> postPage = postRepository.findByTopicIdAndPostStatusAndDeletedFalse(topicId, postStatus, pageable);

    if (postPage.isEmpty())
      return Page.empty(pageable);

    Map<UUID, List<FileMetadata>> filesMap = mapFiles(postPage.getContent());
    Map<UUID, Boolean> likedMap = mapIsLiked(postPage.getContent(), user);

    return postPage.map(
        post -> {
          List<FileMetadata> files = filesMap.getOrDefault(post.getId(), Collections.emptyList());
          return buildPostResponse(
              post,
              files,
              user,
              likedMap.getOrDefault(post.getId(), false),
              true,
              authorizationService.isPostCreator(post, user));
        });
  }

  // ===================== GET POSTS BY USER =====================
  @Transactional
  public Page<PostResponse> getPostsByUserId(UUID userId, Pageable pageable) {
    Page<Post> postPage = postRepository.findAllByAuthor_Id(userId, pageable);
    if (postPage.isEmpty())
      return Page.empty(pageable);

    User currentUser = currentUserService.getCurrentUserEntity();
    Map<UUID, List<FileMetadata>> filesMap = mapFiles(postPage.getContent());
    Map<UUID, Boolean> likedMap = mapIsLiked(postPage.getContent(), currentUser);

    return postPage.map(post -> {
      List<FileMetadata> files = filesMap.getOrDefault(post.getId(), Collections.emptyList());
      return buildPostResponse(
          post,
          files,
          currentUser,
          likedMap.getOrDefault(post.getId(), false),
          authorizationService.canManageTopic(currentUser, post.getTopic()),
          authorizationService.isPostCreator(post, currentUser));
    });
  }

  // ===================== GET MY POSTS =====================
  @Transactional
  public Page<PostResponse> getMyPosts(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Post> postPage = postRepository.findAllByAuthor_Id(user.getId(), pageable);
    if (postPage.isEmpty())
      return Page.empty(pageable);

    Map<UUID, List<FileMetadata>> filesMap = mapFiles(postPage.getContent());
    Map<UUID, Boolean> likedMap = mapIsLiked(postPage.getContent(), user);

    return postPage.map(post -> {
      List<FileMetadata> files = filesMap.getOrDefault(post.getId(), Collections.emptyList());
      return buildPostResponse(
          post,
          files,
          user,
          likedMap.getOrDefault(post.getId(), false),
          authorizationService.canManageTopic(user, post.getTopic()),
          authorizationService.isPostCreator(post, user));
    });
  }

  // ===================== HELPERS =====================

  private PostResponse buildPostResponse(
      Post post,
      List<FileMetadata> files,
      User currentUser,
      Boolean isLiked,
      boolean canManageTopic,
      boolean isPostCreator) {

    // author
    PostAuthorResponse author = null;
    if (post.getAuthor() != null) {
      author = PostAuthorResponse.builder()
          .id(post.getAuthor().getId().toString())
          .fullName(post.getAuthor().getFullName())
          .avatarUrl(post.getAuthor().getAvatarUrl())
          .build();
    }

    // stats (commentCount/viewCount để null ở phase này)
    PostStatsResponse stats = PostStatsResponse.builder()
        .reactionCount(post.getReactionCount() == null ? 0L : post.getReactionCount())
        .commentCount(null)
        .viewCount(null)
        .build();

    // user state
    PostUserStateResponse userState = PostUserStateResponse.builder().isLiked(Boolean.TRUE.equals(isLiked)).build();

    // permissions
    boolean canEdit = isPostCreator || canManageTopic || authorizationService.isAdmin(currentUser);
    boolean canDelete = canEdit;
    PostPermissionsResponse permissions = PostPermissionsResponse.builder()
        .canEdit(canEdit)
        .canDelete(canDelete)
        .canReport(!isPostCreator)
        .build();

    List<String> urls = files == null ? List.of() : files.stream().map(FileMetadata::getUrl).toList();
    List<AttachmentResponse> attachments = toAttachments(files);

    return postMapper.toPostResponse(
        post,
        urls,
        buildExcerpt(post.getContent(), 150),
        author,
        stats,
        userState,
        permissions,
        attachments);
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
        .collect(
            Collectors.groupingBy(
                FileMetadata::getResourceId,
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
        .map(f -> AttachmentResponse.builder().id(f.getId().toString()).url(f.getUrl()).build())
        .toList();
  }

  private String buildExcerpt(String content, int maxLen) {
    if (content == null)
      return "";
    String plain = content.replaceAll("\\s+", " ").trim();
    if (plain.length() <= maxLen)
      return plain;
    return plain.substring(0, maxLen).trim() + "...";
  }
}
