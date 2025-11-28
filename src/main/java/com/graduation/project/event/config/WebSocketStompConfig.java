package com.graduation.project.event.config;

import com.graduation.project.event.service.JwtHandshakeHandler;
import com.graduation.project.event.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtService jwtService;

  public WebSocketStompConfig(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // endpoint used by front-end to connect
    registry
        .addEndpoint("/ws/notification")
        .setAllowedOriginPatterns("*")
        .setHandshakeHandler(new JwtHandshakeHandler(jwtService))
        .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    // Simple broker for topic and queue
    registry.enableSimpleBroker("/topic", "/queue");
    // If scaling, replace enableSimpleBroker with enableStompBrokerRelay(...) for Redis/Rabbit
    // relay
  }
}
