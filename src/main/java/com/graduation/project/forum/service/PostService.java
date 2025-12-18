package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.PostMapper;
import com.graduation.project.forum.repository.PostRepository;
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
  private final CurrentUserService currentUserService;
  private final PostMapper postMapper;
  private final FileService fileService;
  private final AuthorizationService authorizationService;
  private final FileMetadataRepository fileMetadataRepository;
  private final ApplicationEventPublisher publisher;

  @Transactional
  public PostResponse createPost(UUID topicId, PostRequest request) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canCreatePost(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Post post = postMapper.toPost(request, topic, user);
    Post save = postRepository.save(post);

    List<FileMetadata> fileMetadataList =
        fileService.updateFileMetadataList(
            request.getFileMetadataIds(), save.getId(), ResourceType.POST, user.getId());
    List<String> urls = fileMetadataList.stream().map(FileMetadata::getUrl).toList();

    CreatedPostEvent event = CreatedPostEvent.from(post);
    publisher.publishEvent(event);
    return postMapper.toPostResponse(post, urls);
  }

  public PostResponse getOne(UUID id) {
    Post post =
        postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, post.getTopic())
        && !authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);
    return postMapper.toPostResponse(post, urls);
  }

  public Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable) {
    Specification<Post> spec =
        (root, query, criteriaBuilder) -> {
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
    Map<UUID, List<String>> filesMap = mapPostWithFileMetadataIds(posts);

    return posts.map(post -> postMapper.toPostResponse(post, filesMap.get(post.getId())));
  }

  public Page<DetailPostResponse> getApprovedPostsByTopic(UUID topicId, Pageable pageable) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    Page<Post> postPage =
        postRepository.findByTopicIdAndPostStatusAndDeletedFalse(
            topicId, PostStatus.APPROVED, pageable);

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }
    Map<UUID, List<String>> urlsByPostId = mapPostWithFileMetadataIds(postPage);

    boolean canManageTopic = authorizationService.canManageTopic(user, topic);

    return postPage.map(
        post -> {
          boolean isPostCreator = authorizationService.isPostCreator(post, user);
          return DetailPostResponse.from(post, urlsByPostId, canManageTopic, isPostCreator);
        });
  }

  @Transactional
  public PostResponse update(UUID id, PostRequest request) {
    Post post =
        postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    postRepository.save(post);
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    List<FileMetadata> fileMetadataList =
        fileService.updateFileMetadataList(
            request.getFileMetadataIds(), post.getId(), ResourceType.POST, user.getId());
    List<String> urls = fileMetadataList.stream().map(FileMetadata::getUrl).toList();
    return postMapper.toPostResponse(post, urls);
  }

  public void delete(String id) {
    Post post =
        postRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    postRepository.delete(post);
  }

  public PostResponse softDelete(String id) {
    Post post =
        postRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canSoftDeletePost(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    post.setDeleted(true);

    postRepository.save(post);
    return postMapper.toPostResponse(post, Collections.emptyList());
  }

  @Transactional
  public PostResponse upgradePostStatus(UUID postId, PostStatus postStatus) {
    Post post =
        postRepository
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

    List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);

    return postMapper.toPostResponse(post, urls);
  }

  @Transactional
  public Page<PostResponse> searchPostsByTopic(
      UUID topicId, PostStatus postStatus, Pageable pageable) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    return postRepository
        .findByTopicIdAndPostStatusAndDeletedFalse(topicId, postStatus, pageable)
        .map(post -> postMapper.toPostResponse(post, null));
  }

  @Transactional
  public Page<PostResponse> getPostsByUserId(UUID userId, Pageable pageable) {
    Page<Post> postPage = postRepository.findAllByAuthor_Id(userId, pageable);
    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Map<UUID, List<String>> urlsByPostId = mapPostWithFileMetadataIds(postPage);
    return postPage.map(post -> postMapper.toPostResponse(post, urlsByPostId.get(post.getId())));
  }

  @Transactional
  public Page<PostResponse> getMyPosts(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();
    Page<Post> postPage = postRepository.findAllByAuthor_Id(user.getId(), pageable);

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    Map<UUID, List<String>> urlsByPostId = mapPostWithFileMetadataIds(postPage);
    return postPage.map(post -> postMapper.toPostResponse(post, urlsByPostId.get(post.getId())));
  }

  private Map<UUID, List<String>> mapPostWithFileMetadataIds(Page<Post> postPage) {
    List<UUID> postIds = postPage.getContent().stream().map(Post::getId).toList();

    List<FileMetadata> allFiles =
        fileMetadataRepository.findByResourceTypeAndResourceIdIn(ResourceType.POST, postIds);
    return allFiles.stream()
        .collect(
            Collectors.groupingBy(
                FileMetadata::getResourceId,
                Collectors.mapping(FileMetadata::getUrl, Collectors.toList())));
  }
}
