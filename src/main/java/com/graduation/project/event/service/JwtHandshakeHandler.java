package com.graduation.project.event.service;

import com.graduation.project.event.config.StompPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j; // 1. Import Slf4j
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j // 2. Annotation để log
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

    String token = null;

    // 1. Ưu tiên lấy từ Query Param (URL)
    // Ví dụ: ws://localhost:8080/ws/notification?token=eyJhbG...
    try {
      URI uri = request.getURI();
      List<String> tokenParams =
          UriComponentsBuilder.fromUri(uri).build().getQueryParams().get("token");
      if (tokenParams != null && !tokenParams.isEmpty()) {
        token = tokenParams.get(0);
      }
    } catch (Exception e) {
      log.warn("Lỗi khi parse URL query param: {}", e.getMessage());
    }

    // 2. Fallback: Lấy từ Header (Postman/Mobile)
    if (token == null && request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpReq = servletRequest.getServletRequest();
      String auth = httpReq.getHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        token = auth.substring(7);
      }
    }

    // 3. Validate Token & Return Principal
    if (token != null) {
      try {
        String userId = jwtService.getUserIdFromToken(token);
        log.info("WebSocket Handshake thành công cho User ID: {}", userId); // Log success
        return new StompPrincipal(userId);
      } catch (Exception e) {
        // QUAN TRỌNG: Log lỗi để biết tại sao auth fail (hết hạn, sai chữ ký...)
        log.error("WebSocket Handshake thất bại (Token invalid): {}", e.getMessage());
      }
    } else {
      log.warn("Không tìm thấy Token trong URL hoặc Header");
    }

    // Nếu return null/super -> User sẽ là Anonymous -> Không nhận được notification cá nhân
    return super.determineUser(request, wsHandler, attributes);
  }
}
