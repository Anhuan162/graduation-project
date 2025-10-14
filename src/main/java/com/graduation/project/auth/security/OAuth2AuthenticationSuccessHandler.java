package com.graduation.project.auth.security;

import com.graduation.project.auth.repository.OauthAccountRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.RefreshTokenService;
import com.graduation.project.common.entity.OauthAccount;
import com.graduation.project.common.entity.Provider;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.RoleRepository;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepo;
  private final RoleRepository roleRepo;
  private final OauthAccountRepository oauthRepo;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshService;
  private final String redirectUri; // from properties

  public OAuth2AuthenticationSuccessHandler(
      UserRepository userRepo,
      RoleRepository roleRepo,
      OauthAccountRepository oauthRepo,
      JwtUtils jwtUtils,
      RefreshTokenService refreshService,
      @Value("${app.oauth2.authorizedRedirectUri}") String redirectUri) {
    this.userRepo = userRepo;
    this.roleRepo = roleRepo;
    this.oauthRepo = oauthRepo;
    this.jwtUtils = jwtUtils;
    this.refreshService = refreshService;
    this.redirectUri = redirectUri;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
    String provider = "LOCAL";
    if (authentication
        instanceof
        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
      provider = token.getAuthorizedClientRegistrationId().toUpperCase(); // "GOOGLE" or "FACEBOOK"
    }

    String providerUserId =
        provider.equals("GOOGLE")
            ? (String) oauthUser.getAttribute("sub")
            : (String) oauthUser.getAttribute("id"); // google uses "sub"; facebook uses "id"
    String email = (String) oauthUser.getAttributes().get("email");
    if (email == null) email = providerUserId + "@" + provider.toLowerCase() + ".oauth";

    String name = (String) oauthUser.getAttribute("name");
    String picture = (String) oauthUser.getAttribute("picture");

    log.info("provider = " + provider);
    log.info("email = " + email);

    Provider providerEnum = Provider.valueOf(provider);
    // Try find oauth account
    Optional<OauthAccount> accOpt =
        oauthRepo.findByProviderAndProviderUserId(providerEnum, providerUserId);
    User user;
    if (accOpt.isPresent()) {
      user = accOpt.get().getUser();
    } else {
      Role userRole =
          roleRepo
              .findByName("USER")
              .orElseThrow(() -> new RuntimeException("Role USER not found"));
      // Try find user by email
      String finalEmail = email;
      user =
          userRepo
              .findByEmail(email)
              .orElseGet(
                  () -> {
                    User u = new User();
                    u.setEmail(finalEmail);
                    u.setProvider(Provider.GOOGLE); // or FACEBOOK depending
                    u.setEnabled(true);
                    u.setFullName((String) oauthUser.getAttribute("name"));
                    // assign default USER role — assume role exists in DB
                    u.getRoles().add(userRole);
                    return userRepo.save(u);
                  });

      OauthAccount acc = new OauthAccount();
      acc.setProvider(Provider.GOOGLE);
      acc.setProviderUserId(providerUserId);
      acc.setUser(user);
      oauthRepo.save(acc);
    }

    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), roles);
    String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
    refreshService.createRefreshTokenForUser(user, refreshToken);

    // Safer way: use cookies instead of query params
    Cookie accessCookie = new Cookie("accessToken", accessToken);
    accessCookie.setHttpOnly(true);
    accessCookie.setPath("/");
    accessCookie.setMaxAge(3600);
    response.addCookie(accessCookie);

    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(86400);
    response.addCookie(refreshCookie);

    response.sendRedirect(redirectUri);
  }
}
