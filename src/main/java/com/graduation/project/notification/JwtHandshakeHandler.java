package com.graduation.project.notification;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * HandshakeHandler that maps an Authorization: Bearer <token> header to a Principal so that
 * convertAndSendToUser(personId, ...) will work.
 *
 * <p>You need to implement JwtService#getUserIdFromToken(token) to return the user's id (String).
 */
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

  private final JwtService jwtService;

  public JwtHandshakeHandler(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected Principal determineUser(
      ServerHttpRequest request,
      org.springframework.web.socket.WebSocketHandler wsHandler,
      Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpReq = servletRequest.getServletRequest();
      String auth = httpReq.getHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7);
        try {
          String userId = jwtService.getUserIdFromToken(token); // implement in your JwtService
          return new StompPrincipal(userId);
        } catch (Exception e) {
          // invalid token -> anonymous principal
        }
      }
    }
    return super.determineUser(request, wsHandler, attributes);
  }
}
