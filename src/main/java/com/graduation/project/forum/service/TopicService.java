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
import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.mapper.TopicMapper;
import com.graduation.project.forum.repository.CategoryRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

  private final TopicRepository topicRepository;
  private final CategoryRepository categoryRepository;
  private final CurrentUserService currentUserService;
  private final TopicMapper topicMapper;
  private final AuthorizationService authorizationService;
  private final ApplicationEventPublisher publisher;

  private static final String CREATED_AT_FIELD = "createdAt";

  private Specification<Topic> getTopicVisibilitySpec(User user) {
    return (root, query, cb) -> {
      if (authorizationService.isAdmin(user)) {
        return cb.conjunction();
      }

      Predicate isPublic = cb.equal(root.get("topicVisibility"), TopicVisibility.PUBLIC);

      Subquery<Long> memberSubquery = query.subquery(Long.class);
      Root<TopicMember> memberRoot = memberSubquery.from(TopicMember.class);
      memberSubquery.select(cb.literal(1L));
      memberSubquery.where(
          cb.equal(memberRoot.get("topic"), root),
          cb.equal(memberRoot.get("user").get("id"), user.getId()),
          cb.isTrue(memberRoot.get("approved")));

      return cb.or(isPublic, cb.exists(memberSubquery));
    };
  }

  @Transactional
  public TopicResponse create(UUID categoryId, TopicRequest request) {
    Category category = categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canCreateTopic(category, user)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    Topic topic = topicMapper.toTopic(request, category, user);
    topicRepository.save(topic);

    ActivityLogDTO activityLogDTO = ActivityLogDTO.from(
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

  @Transactional(readOnly = true)
  public DetailTopicResponse getOneTopic(UUID topicId) {
    User user = currentUserService.getCurrentUserEntity();

    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    if (!authorizationService.canViewTopic(topic, user)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    boolean isTopicCreator = authorizationService.isTopicCreator(user, topic);
    boolean isTopicManager = authorizationService.isTopicManager(user, topic);
    boolean isTopicMember = authorizationService.isTopicMember(user, topic);

    DetailTopicResponse.CurrentUserContext currentUserContext = DetailTopicResponse.CurrentUserContext
        .from(isTopicCreator, isTopicMember, isTopicManager);

    return DetailTopicResponse.from(topic, currentUserContext);
  }

  @Transactional(readOnly = true)
  public Page<TopicResponse> searchTopics(SearchTopicRequest request, Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();

    Specification<Topic> visibilitySpec = getTopicVisibilitySpec(user);

    Specification<Topic> filterSpec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.isFalse(root.get("deleted")));
      return cb.and(predicates.toArray(new Predicate[0]));
    };

    return topicRepository.findAll(visibilitySpec.and(filterSpec), pageable)
        .map(topicMapper::toTopicResponse);
  }

  @Transactional(readOnly = true)
  public Page<TopicResponse> getByCategory(UUID categoryId, Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();

    Specification<Topic> categorySpec = (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);

    Specification<Topic> notDeletedSpec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

    Specification<Topic> securitySpec = getTopicVisibilitySpec(user);

    Specification<Topic> finalSpec = Specification.where(categorySpec)
        .and(notDeletedSpec)
        .and(securitySpec);

    return topicRepository.findAll(finalSpec, pageable)
        .map(topicMapper::toTopicResponse);
  }

  @Transactional(readOnly = true)
  public Page<TopicResponse> getPostableTopics(Pageable pageable) {
    User user = currentUserService.getCurrentUserEntity();

    if (authorizationService.isAdmin(user)) {
      return topicRepository.findAllByDeletedFalseOrderByCreatedAtDesc(pageable)
          .map(topicMapper::toTopicResponse);
    }

    return topicRepository.findPostableTopicsByUser(user.getId(), pageable)
        .map(topicMapper::toTopicResponse);
  }

  @Transactional
  public TopicResponse update(UUID topicId, TopicRequest request) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    topic.setTitle(request.getTitle());
    topic.setContent(request.getContent());
    try {
      topic.setTopicVisibility(TopicVisibility.valueOf(request.getTopicVisibility()));
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.INVALID_REQUEST);
    }
    topic.setLastModifiedAt(Instant.now());

    topicRepository.save(topic);
    return topicMapper.toTopicResponse(topic);
  }

  @Transactional
  public void delete(UUID id) {
    Topic topic = topicRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.isAdmin(user)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    topicRepository.delete(topic);
  }

  @Transactional
  public TopicResponse softDelete(UUID id) {
    Topic topic = topicRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(user, topic)) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    topic.setDeleted(true);
    topicRepository.save(topic);
    return topicMapper.toTopicResponse(topic);
  }
}
