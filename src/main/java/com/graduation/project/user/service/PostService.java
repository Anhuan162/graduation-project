package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.PostRepository;
import com.graduation.project.common.repository.TopicRepository;
import com.graduation.project.common.service.AuthorizationService;
import com.graduation.project.common.service.FileService;
import com.graduation.project.user.dto.PostRequest;
import com.graduation.project.user.dto.PostResponse;
import com.graduation.project.user.mapper.PostMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

  @Transactional
  public PostResponse createPost(UUID topicId, PostRequest request) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!canCreatePost(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Post post = postMapper.toPost(request, topic, user);
    Post save = postRepository.save(post);

    List<FileMetadata> fileMetadataList =
        fileService.updateFileMetadataList(request.getFileMetadataIds(), save, user.getId());
    List<String> urls = fileMetadataList.stream().map(FileMetadata::getUrl).toList();
    return postMapper.toPostResponse(post, urls);
  }

  private boolean canCreatePost(Topic topic, User user) {
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      return true;
    }

    return topic.getTopicMembers().stream()
        .anyMatch(m -> m.getUser().getId().equals(user.getId()) && m.isApproved());
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

  public List<PostResponse> getByTopic(UUID topicId) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canViewTopic(topic, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    return postRepository.findByTopicId(topicId).stream()
        .map(
            post -> {
              List<String> urls = fileService.getFileMetadataIds(post.getId(), ResourceType.POST);
              return postMapper.toPostResponse(post, urls);
            })
        .toList();
  }

  @Transactional
  public PostResponse update(UUID id, PostRequest request) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    postRepository.save(post);
    var user = currentUserService.getCurrentUserEntity();

    if (authorizationService.isPostCreator(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    List<FileMetadata> fileMetadataList =
        fileService.updateFileMetadataList(request.getFileMetadataIds(), post, user.getId());
    List<String> urls = fileMetadataList.stream().map(FileMetadata::getUrl).toList();
    return postMapper.toPostResponse(post, urls);
  }

  public void delete(String id) {
    Post post =
        postRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canDeletePost(post, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    postRepository.delete(post);
  }

  @Transactional
  public PostResponse approvePost(UUID postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (authorizationService.canNotManageTopic(currentUser, post.getTopic())) {
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

    if (authorizationService.canNotManageTopic(user, topic)) {
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

    if (authorizationService.canNotManageTopic(currentUser, post.getTopic())) {
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
