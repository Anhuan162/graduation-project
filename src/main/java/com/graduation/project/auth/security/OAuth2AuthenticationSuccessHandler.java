package com.graduation.project.auth.security;

import com.graduation.project.auth.repository.OauthAccountRepository;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.TokenService;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.OauthAccount;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepo;
  private final RoleRepository roleRepo;
  private final OauthAccountRepository oauthRepo;
  private final TokenService tokenService;
  private final String redirectUri; // from properties

  public OAuth2AuthenticationSuccessHandler(
      UserRepository userRepo,
      RoleRepository roleRepo,
      OauthAccountRepository oauthRepo,
      TokenService tokenService,
      @Value("${app.oauth2.authorizedRedirectUri}") String redirectUri) {
    this.userRepo = userRepo;
    this.roleRepo = roleRepo;
    this.oauthRepo = oauthRepo;
    this.tokenService = tokenService;
    this.redirectUri = redirectUri;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
    String provider = "LOCAL";

    // Lấy Provider ID (Google/Facebook)
    if (authentication instanceof OAuth2AuthenticationToken token) {
      provider = token.getAuthorizedClientRegistrationId().toUpperCase();
    }

    // Lấy thông tin User từ Google/Facebook
    String providerUserId = provider.equals("GOOGLE")
        ? (String) oauthUser.getAttribute("sub")
        : (String) oauthUser.getAttribute("id");

    String email = oauthUser.getAttribute("email");
    if (email == null)
      email = providerUserId + "@" + provider.toLowerCase() + ".oauth";

    Provider providerEnum = Provider.valueOf(provider); // Lấy Enum động

    // Xử lý logic tìm hoặc tạo user
    Optional<OauthAccount> accOpt = oauthRepo.findByProviderAndProviderUserId(providerEnum, providerUserId);
    User user;

    if (accOpt.isPresent()) {
      user = accOpt.get().getUser();
    } else {
      Role userRole = roleRepo
          .findByName("USER")
          .orElseThrow(() -> new RuntimeException("Role USER not found"));

      String finalEmail = email;
      user = userRepo
          .findByEmail(email)
          .orElseGet(
              () -> {
                User u = new User();
                u.setEmail(finalEmail);
                u.setProvider(providerEnum);
                u.setEnabled(true);
                u.setFullName(oauthUser.getAttribute("name"));
                u.setRegistrationDate(LocalDateTime.now());
                u.getRoles().add(userRole);
                return userRepo.save(u);
              });

      OauthAccount acc = new OauthAccount();
      acc.setProvider(providerEnum);
      acc.setProviderUserId(providerUserId);
      acc.setUser(user);
      oauthRepo.save(acc);
    }

    // Tạo Token
    String accessToken = tokenService.generateToken(user, false);
    String refreshToken = tokenService.generateToken(user, true);

    // Lưu Cookie
    addCookie(response, "accessToken", accessToken, 3600);
    addCookie(response, "refreshToken", refreshToken, 86400);

    response.sendRedirect(redirectUri);
  }

  private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from(name, value)
        .httpOnly(true)
        .path("/")
        .maxAge(maxAge)
        .sameSite("Lax")
        .secure(false) // Set to true for production (HTTPS)
        .build();
    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
