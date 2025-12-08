package com.graduation.project.event.listener;

import com.graduation.project.announcement.dto.AnnouncementCreatedEvent;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationMessageDTO; // DTO riêng cho thông báo
import com.graduation.project.event.producer.StreamProducer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

  private final StreamProducer streamProducer;
  private final UserRepository userRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleAnnouncementCreatedEvent(AnnouncementCreatedEvent event) {

    List<User> receivedUsers = userRepository.findAllByClassCodeIn(event.getAllClassroomCodes());
    Set<UUID> receiverUserIds = receivedUsers.stream().map(User::getId).collect(Collectors.toSet());

    NotificationMessageDTO notificationDTO =
        NotificationMessageDTO.builder()
            .relatedId(event.getAnnouncementId())
            .type(ResourceType.ANNOUNCEMENT)
            .title(event.getTitle())
            .content(event.getContent())
            .createdAt(LocalDateTime.now())
            .senderId(event.getActorId())
            .senderName(event.getActorEmail())
            .receiverIds(receiverUserIds)
            .build();

    EventEnvelope eventEnvelope =
        EventEnvelope.from(EventType.NOTIFICATION, notificationDTO, "ANNOUNCEMENT");
    streamProducer.publish(eventEnvelope);
  }
}
