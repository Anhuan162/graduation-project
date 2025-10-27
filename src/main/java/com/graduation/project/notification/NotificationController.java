package com.graduation.project.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationController {
  private final SimpMessagingTemplate messagingTemplate;

  // client gửi message lên (nếu cần)
  @MessageMapping("/hello")
  public void handleMessage(String message) {
    System.out.println("Client sent: " + message);
  }

  // Gửi realtime đến user cụ thể (VD khi admin tạo announcement)
  public void sendToUser(String userId, Object payload) {
    messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", payload);
  }

  // Broadcast đến tất cả user
  public void broadcast(Object payload) {
    messagingTemplate.convertAndSend("/topic/notifications", payload);
  }
}
