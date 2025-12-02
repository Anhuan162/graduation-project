package com.graduation.project.event.service;

import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.event.constant.NotificationStatus;
import com.graduation.project.event.dto.NotificationMessageDTO;
import com.graduation.project.event.dto.UserNotificationResponse;
import java.time.LocalDateTime;

import com.graduation.project.event.entity.NotificationEvent;
import com.graduation.project.event.entity.UserNotification;
import com.graduation.project.event.repository.NotificationEventRepository;
import com.graduation.project.event.repository.UserNotificationRepository;
import com.graduation.project.common.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

  private final NotificationEventRepository notificationEventRepository;
  private final UserNotificationRepository userNotificationRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public void handleNotification(NotificationMessageDTO dto) {
    User sender = userRepository.findById(dto.getSenderId()).orElse(null);

    NotificationEvent event =
        NotificationEvent.builder()
            .title(dto.getTitle())
            .content(dto.getContent())
            .type(dto.getType())
            .relatedId(dto.getRelatedId())
            .createdBy(sender)
            .createdAt(LocalDateTime.now())
            .build();
    notificationEventRepository.save(event);

    dto.getReceiverIds()
        .forEach(
            userId ->
                userRepository
                    .findById(userId)
                    .ifPresent(
                        user -> {
                          UserNotification userNotif =
                              UserNotification.builder()
                                  .notificationEvent(event)
                                  .user(user)
                                  .isRead(false)
                                  .notificationStatus(NotificationStatus.SENT)
                                  .build();
                          userNotificationRepository.save(userNotif);
                          var response =
                              UserNotificationResponse.toUserNotificationResponse(userNotif);

                          try {
                            messagingTemplate.convertAndSendToUser(
                                String.valueOf(userId), "/queue/notifications", response);
                          } catch (Exception e) {
                            log.error(
                                "Failed to send STOMP message to {}: {}", response, e.getMessage());
                          }
                        }));
  }
}
