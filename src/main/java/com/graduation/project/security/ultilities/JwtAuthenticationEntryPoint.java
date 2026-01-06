package com.graduation.project.security.ultilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
      throws IOException {
      System.out.println("ERROR AUTHENTICATION: " + ex.getMessage());
    ex.printStackTrace();
    ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

    res.setStatus(errorCode.getHttpStatusCode().value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ApiResponse<?> apiResponse =
        ApiResponse.builder().code(errorCode.getCode()).message(errorCode.getMessage()).build();

    ObjectMapper objectMapper = new ObjectMapper();

    res.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    res.flushBuffer();
  }
}
