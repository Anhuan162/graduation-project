package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.dto.DetailTopicResponse;
import com.graduation.project.forum.dto.SearchTopicRequest;
import com.graduation.project.forum.dto.TopicRequest;
import com.graduation.project.forum.dto.TopicResponse;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.mapper.TopicMapper;
import com.graduation.project.forum.repository.CategoryRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

  private final TopicRepository topicRepository;
  private final CategoryRepository categoryRepository;
  private final CurrentUserService currentUserService;
  private final TopicMapper topicMapper;
  private final AuthorizationService authorizationService;
  private final ApplicationEventPublisher publisher;

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
            "Người dùng " + user.getEmail() + " đã tạo topic mới: " + topic.getTitle(),
            "127.0.0.1");
    publisher.publishEvent(activityLogDTO);
    return topicMapper.toTopicResponse(topic);
  }

  public DetailTopicResponse getOneTopic(UUID topicId) {
    User user = currentUserService.getCurrentUserEntity();
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    boolean isTopicCreator = authorizationService.isTopicCreator(user, topic);
    boolean isTopicManager = authorizationService.isTopicManager(user, topic);
    boolean isTopicMember = authorizationService.isTopicMember(user, topic);

    DetailTopicResponse.CurrentUserContext currentUserContext =
        DetailTopicResponse.CurrentUserContext.from(isTopicCreator, isTopicMember, isTopicManager);
    return DetailTopicResponse.from(topic, currentUserContext);
  }

  public Page<TopicResponse> searchTopics(SearchTopicRequest request, Pageable pageable) {
    Specification<Topic> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          Join<Object, Object> categoryJoin = root.join("category", JoinType.LEFT);

          if (Objects.nonNull(request.getCategoryId())) {
            predicates.add(cb.equal(categoryJoin.get("id"), request.getCategoryId()));
          }

          if (Objects.nonNull(request.getKeyword()) && !request.getKeyword().trim().isEmpty()) {
            String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";

            Predicate hasTitle = cb.like(cb.lower(root.get("title")), pattern);
            Predicate hasContent = cb.like(cb.lower(root.get("content")), pattern);

            predicates.add(cb.or(hasTitle, hasContent));
          }

          if (Objects.nonNull(request.getVisibility())) {
            predicates.add(
                cb.equal(
                    root.get("visibility").as(TopicVisibility.class), request.getVisibility()));
          }

          if (Objects.nonNull(request.getFromDate())) {
            predicates.add(
                cb.greaterThanOrEqualTo(
                    root.get("createdAt"), request.getFromDate().atStartOfDay()));
          }

          if (Objects.nonNull(request.getToDate())) {
            predicates.add(
                cb.lessThanOrEqualTo(
                    root.get("createdAt"), request.getToDate().atTime(23, 59, 59)));
          }

          Objects.requireNonNull(query).orderBy(cb.desc(root.get("createdAt")));

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    return topicRepository.findAll(spec, pageable).map(topicMapper::toTopicResponse);
  }

  public Page<TopicResponse> getByCategory(UUID categoryId, Pageable pageable) {
    return topicRepository
        .findByCategoryIdAndIsDeletedIsFalse(categoryId, pageable)
        .map(topicMapper::toTopicResponse);
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
    topic.setLastModifiedAt(LocalDateTime.now());

    topicRepository.save(topic);
    return topicMapper.toTopicResponse(topic);
  }

  public void delete(UUID id) {
    Topic topic =
        topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topicRepository.delete(topic);
  }

  public TopicResponse softDelete(UUID id) {
    Topic topic =
        topicRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User user = currentUserService.getCurrentUserEntity();
    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topic.setDeleted(true);
    topicRepository.save(topic);
    return topicMapper.toTopicResponse(topic);
  }
}
