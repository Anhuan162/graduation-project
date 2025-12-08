package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.dto.PostRequest;
import com.graduation.project.forum.dto.PostResponse;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.PostMapper;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    return postMapper.toPostResponse(post, urls);
  }

  public PostResponse getOne(UUID id) {
    Post post =
        postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canViewTopic(post.getTopic(), user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);
    return postMapper.toPostResponse(post, urls);
  }

  public List<PostResponse> getAll() {
    var user = currentUserService.getCurrentUserEntity();

    return postRepository.findAll().stream()
        .filter(post -> authorizationService.canViewTopic(post.getTopic(), user))
        .map(
            post -> {
              List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);
              return postMapper.toPostResponse(post, urls);
            })
        .toList();
  }

  public Page<PostResponse> getPostsByTopic(UUID topicId, Pageable pageable) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    Page<Post> postPage = postRepository.findByTopicId(topicId, pageable);

    if (postPage.isEmpty()) {
      return Page.empty(pageable);
    }

    List<UUID> postIds = postPage.getContent().stream().map(Post::getId).toList();

    // 3. Fetch all files related to these posts
    List<FileMetadata> allFiles =
        fileMetadataRepository.findByResourceTypeAndResourceIdIn(ResourceType.POST, postIds);

    // 4. Group files by Post ID for easy access
    Map<UUID, List<String>> urlsByPostId =
        allFiles.stream()
            .collect(
                Collectors.groupingBy(
                    FileMetadata::getResourceId,
                    Collectors.mapping(FileMetadata::getUrl, Collectors.toList())));

    // 5. Map Entities to DTOs and inject the URLs
    return postPage.map(
        post ->
            PostResponse.builder()
                .id(post.getId().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .topicId(post.getTopic().getId().toString()) // Assuming Topic has an ID
                .createdById(post.getAuthor() != null ? post.getAuthor().getId().toString() : null)
                .urls(urlsByPostId.getOrDefault(post.getId(), Collections.emptyList()))
                .build());
  }

  @Transactional
  public PostResponse update(UUID id, PostRequest request) {
    Post post =
        postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    postRepository.save(post);
    var user = currentUserService.getCurrentUserEntity();

    if (authorizationService.isPostCreator(post, user)) {
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

  public void softDelete(String id) {
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
  }

  @Transactional
  public PostResponse approvePost(UUID postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(currentUser, post.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    post.setPostStatus(PostStatus.APPROVED);
    post.setApprovedBy(currentUser);
    post.setApprovedAt(LocalDateTime.now());

    postRepository.save(post);

    List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);

    return postMapper.toPostResponse(post, urls);
  }

  @Transactional
  public List<PostResponse> getPendingByTopic(UUID topicId) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    return postRepository.findByTopicIdAndPostStatus(topicId, PostStatus.PENDING).stream()
        .map(post -> postMapper.toPostResponse(post, null))
        .toList();
  }

  public List<PostResponse> getApprovedByTopic(UUID topicId) {
    return postRepository.findByTopicIdAndPostStatus(topicId, PostStatus.APPROVED).stream()
        .map(
            post ->
                postMapper.toPostResponse(
                    post, fileService.getFileMetadataIds(post.getId(), ResourceType.POST)))
        .toList();
  }

  public List<PostResponse> getRejectedByTopic(UUID topicId) {
    return postRepository.findByTopicIdAndPostStatus(topicId, PostStatus.REJECTED).stream()
        .map(
            post ->
                postMapper.toPostResponse(
                    post, fileService.getFileMetadataIds(post.getId(), ResourceType.POST)))
        .toList();
  }

  @Transactional
  public PostResponse rejectPost(UUID postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(currentUser, post.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    post.setPostStatus(PostStatus.REJECTED);
    post.setApprovedBy(currentUser);
    post.setApprovedAt(LocalDateTime.now());
    postRepository.save(post);

    List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);

    return postMapper.toPostResponse(post, urls);
  }
}
