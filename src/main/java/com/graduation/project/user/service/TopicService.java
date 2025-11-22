package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.CategoryRepository;
import com.graduation.project.common.repository.TopicRepository;
import com.graduation.project.common.service.AuthorizationService;
import com.graduation.project.user.dto.TopicRequest;
import com.graduation.project.user.dto.TopicResponse;
import com.graduation.project.user.mapper.TopicMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

  private final TopicRepository topicRepository;
  private final CategoryRepository categoryRepository;
  private final CurrentUserService currentUserService;
  private final TopicMapper topicMapper;
  private final AuthorizationService authorizationService;

  public TopicResponse create(UUID categoryId, TopicRequest request) {
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
    var user = currentUserService.getCurrentUserEntity();
    authorizationService.checkCanCreateTopic(category, user);

    Topic topic = topicMapper.toTopic(request, category, user);
    topicRepository.save(topic);

    return topicMapper.toTopicResponse(topic);
  }

  public TopicResponse getOneTopic(String topicId) {
    Topic topic =
        topicRepository
            .findById(UUID.fromString(topicId))
            .orElseThrow(() -> new RuntimeException("Topic not found"));
    return topicMapper.toTopicResponse(topic);
  }

  public List<TopicResponse> getAll() {
    return topicRepository.findAll().stream().map(topicMapper::toTopicResponse).toList();
  }

  public List<TopicResponse> getByCategory(UUID categoryId) {
    return topicRepository.findByCategoryId(categoryId).stream()
        .map(topicMapper::toTopicResponse)
        .toList();
  }

  public TopicResponse update(String topicId, TopicRequest request) {
    Topic topic =
        topicRepository
            .findById(UUID.fromString(topicId))
            .orElseThrow(() -> new RuntimeException("Topic not found"));
    User user = currentUserService.getCurrentUserEntity();
    if (authorizationService.canNotManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topic.setTitle(request.getTitle());
    topic.setContent(request.getContent());

    topicRepository.save(topic);
    return topicMapper.toTopicResponse(topic);
  }

  public void delete(String id) {
    Topic topic =
        topicRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Topic not found"));
    User user = currentUserService.getCurrentUserEntity();
    if (authorizationService.canNotManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topicRepository.delete(topic);
  }
}
