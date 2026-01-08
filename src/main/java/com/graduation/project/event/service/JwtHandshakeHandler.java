package com.graduation.project.event.service;

import com.graduation.project.event.config.StompPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.WebUtils;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

  private final JwtService jwtService;

  @Override
  protected Principal determineUser(
      ServerHttpRequest request,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {

    String token = null;

    // 1. Primary: Get from HttpOnly Cookie (For Web/Next.js)
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
      Cookie cookie = WebUtils.getCookie(httpServletRequest, "accessToken");
      if (cookie != null) {
        token = cookie.getValue();
      }
    }

    // 2. Fallback: Get from Authorization Header (For Mobile App / Postman)
    // Query Param removed to fix security vulnerability (CodeRabbit)
    if (token == null && request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpReq = servletRequest.getServletRequest();
      String auth = httpReq.getHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        token = auth.substring(7);
      }
    }

    // 3. Validate Token
    if (token != null) {
      try {
        String userId = jwtService.getUserIdFromToken(token);
        log.info("WebSocket Handshake Success. UserID: {}", userId);
        return new StompPrincipal(userId);
      } catch (Exception e) {
        log.error("WebSocket Handshake Failed: Invalid Token");
        throw new IllegalArgumentException("Unauthorized: Invalid Token", e);
      }
    }

    // 4. Reject Anonymous
    log.error("WebSocket Handshake Failed: No Token Found");
    throw new IllegalArgumentException("Unauthorized: No Token Found");
  }
}
