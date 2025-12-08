package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.dto.TopicRequest;
import com.graduation.project.forum.dto.TopicResponse;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.TopicMapper;
import com.graduation.project.forum.repository.CategoryRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
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
  private final StreamProducer streamProducer;

  public TopicResponse create(UUID categoryId, TopicRequest request) {
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    var user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canCreateTopic(category, user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Topic topic = topicMapper.toTopic(request, category, user);
    topicRepository.save(topic);
    ActivityLogDTO activityLogDTO =
        ActivityLogDTO.from(
            user.getId(),
            "CREATE",
            "FORUM",
            ResourceType.TOPIC,
            topic.getId(),
            "User " + user.getEmail() + " created new topic: " + topic.getTitle(),
            "127.0.0.1");
    EventEnvelope eventEnvelope =
        EventEnvelope.from(
            EventType.ACTIVITY_LOG, activityLogDTO, String.valueOf(ResourceType.TOPIC));
    streamProducer.publish(eventEnvelope);
    return topicMapper.toTopicResponse(topic);
  }

  public TopicResponse getOneTopic(UUID topicId) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
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

  public TopicResponse update(UUID topicId, TopicRequest request) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topic.setTitle(request.getTitle());
    topic.setContent(request.getContent());
    topic.setTopicVisibility(TopicVisibility.valueOf(request.getTopicVisibility()));
    //    topic.setLastModifiedDateTime(LocalDateTime.now());

    topicRepository.save(topic);
    return topicMapper.toTopicResponse(topic);
  }

  public void delete(UUID id) {
    Topic topic =
        topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topicRepository.delete(topic);
  }

  public void softDelete(UUID id) {
    Topic topic =
        topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topic.setDeleted(true);
    topicRepository.save(topic);
  }
}
