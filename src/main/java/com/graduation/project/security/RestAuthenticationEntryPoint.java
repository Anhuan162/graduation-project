package com.graduation.project.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
      throws IOException {
    res.setContentType("application/json");
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    res.getWriter().write("{\"error\":\"Unauthorized\"}");
  }
}
