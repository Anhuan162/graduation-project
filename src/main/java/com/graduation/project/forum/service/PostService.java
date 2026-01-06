package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.dto.FileResponse;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.mapper.FileMetadataMapper;
import com.graduation.project.common.service.DriveService;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Reaction;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.PostMapper;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.forum.dto.PostAcceptedFilterRequest;
import com.graduation.project.forum.dto.PostAcceptedResonse;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
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
    List<FileMetadataResponse> attachments = fileMetadataList.stream()
        .map(fileMetadataMapper::toFileMetadataResponse)
        .toList();

    CreatedPostEvent event = CreatedPostEvent.from(post);
    publisher.publishEvent(event);
    return postMapper.toPostResponse(post, attachments, false);
  }

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

    return posts.map(post -> postMapper.toPostResponse(
        post,
        filesMap.getOrDefault(post.getId(), List.of()),
        likedMap.getOrDefault(post.getId(), false)));
  }

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
    Map<UUID, Boolean> likedMap = user != null
        ? mapPostWithUserLiked(postPage, user.getId())
        : Collections.emptyMap();

    boolean canManageTopic = authorizationService.canManageTopic(user, topic);

    return postPage.map(
        post -> {
          boolean isPostCreator = authorizationService.isPostCreator(post, user);
          return DetailPostResponse.from(post, filesMap, canManageTopic, isPostCreator,
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
    List<FileMetadata> fileMetadataList = fileService.updateFileMetadataList(
        request.getFileMetadataIds(), post.getId(), ResourceType.POST, user.getId());
    List<FileMetadataResponse> attachments = fileMetadataList.stream()
        .map(fileMetadataMapper::toFileMetadataResponse)
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

  @Transactional
  public Page<PostResponse> getPostsByUserId(UUID userId, Pageable pageable) {
    Page<Post> postPage = postRepository.findAllByAuthor_Id(userId, pageable);
    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Map<UUID, List<FileMetadataResponse>> filesMap = mapPostWithFileMetadata(postPage);
    Map<UUID, Boolean> likedMap = mapPostWithUserLiked(postPage, userId);
    return postPage.map(post -> postMapper.toPostResponse(
        post,
        filesMap.getOrDefault(post.getId(), List.of()),
        likedMap.getOrDefault(post.getId(), false)));
  }

  @Transactional
  public Page<PostResponse> getMyPosts(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Post> postPage = postRepository.findAllByAuthor_Id(user.getId(), pageable);

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Map<UUID, List<FileMetadataResponse>> filesMap = mapPostWithFileMetadata(postPage);
    Map<UUID, Boolean> likedMap = mapPostWithUserLiked(postPage, user.getId());
    return postPage.map(post -> postMapper.toPostResponse(
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
                Collectors.mapping(fileMetadataMapper::toFileMetadataResponse, Collectors.toList())));
  }

  private Map<UUID, Boolean> mapPostWithUserLiked(Page<Post> postPage, UUID userId) {
    List<UUID> postIds = postPage.getContent().stream().map(Post::getId).toList();
    List<Reaction> reactions = reactionRepository.findByUser_IdAndTargetTypeAndTargetIdIn(
        userId, TargetType.POST, postIds);
    return reactions.stream()
        .collect(Collectors.toMap(
            Reaction::getTargetId,
            r -> true));
  }

  private List<FileMetadataResponse> getFileMetadataResponses(UUID postId) {
    List<String> urls = fileService.getFileMetadataIds(postId, ResourceType.POST);
    List<FileMetadata> files = fileMetadataRepository.findByResourceTypeAndResourceIdIn(
        ResourceType.POST, List.of(postId));
    return files.stream()
        .map(fileMetadataMapper::toFileMetadataResponse)
        .toList();
  }

  private Boolean checkUserLiked(UUID userId, UUID postId) {
    return reactionRepository.findByUser_IdAndTargetIdAndTargetType(
        userId, postId, TargetType.POST).isPresent();
  }

  public List<PostAcceptedResonse> searchPostAccepted(PostAcceptedFilterRequest postAcceptedRequest) {
    List<Post> posts = postRepository.getPostAccepted(postAcceptedRequest);
    return posts.stream().map(Post::toPostAcceptedResonse).toList();
  }

  public FileResponse upLoadPostAndCommentToDrive(PostAcceptedSelectList postAcceptedSelectList) throws IOException {
    StringBuilder res = new StringBuilder();

    for (PostAcceptedSelect post : postAcceptedSelectList.getPostAcceptedSelects()) {
      // Thêm thông tin bài post
      res.append("#").append(post.getTitle());
      res.append(post.getContent()).append("\n");

      // Comments
      for (CommentAcceptedResponse comment : post.getComments()) {
        res.append(comment.getContent()).append("\n");
      }
      res.append("\n\n");
    }
    return driveService.uploadTextToDrive(postAcceptedSelectList.getNameFile(), res.toString());
  }

}
