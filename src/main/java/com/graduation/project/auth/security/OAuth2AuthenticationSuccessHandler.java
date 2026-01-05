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
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
    String provider = "LOCAL";

    // Lấy Provider ID (Google/Facebook)
    if (authentication instanceof OAuth2AuthenticationToken token) {
      provider = token.getAuthorizedClientRegistrationId().toUpperCase();
    }
    Provider providerEnum = Provider.valueOf(provider);

    String providerUserId = provider.equals("GOOGLE")
        ? (String) oauthUser.getAttribute("sub")
        : (String) oauthUser.getAttribute("id");

    String email = oauthUser.getAttribute("email");
    if (email == null) email = providerUserId + "@" + provider.toLowerCase() + ".oauth";

    String fullName = extractFullName(oauthUser, email);

    String avatarUrl = (String) oauthUser.getAttribute("picture");
    if (avatarUrl == null || avatarUrl.isBlank()) {
      avatarUrl = "https://ui-avatars.com/api/?name=" + fullName.replace(" ", "+") + "&size=128&background=random";
    }

    User user;
    Optional<OauthAccount> accOpt = oauthRepo.findByProviderAndProviderUserId(providerEnum, providerUserId);

    if (accOpt.isPresent()) {
      user = accOpt.get().getUser();
      user = syncUserData(user, fullName, avatarUrl);
    } else {
      Optional<User> userByEmail = userRepo.findByEmail(email);

      if (userByEmail.isPresent()) {
        user = userByEmail.get();
        user = syncUserData(user, fullName, avatarUrl);
      } else {
        user = createNewUser(email, fullName, avatarUrl, providerEnum);
      }

      OauthAccount newAcc = new OauthAccount();
      newAcc.setProvider(providerEnum);
      newAcc.setProviderUserId(providerUserId);
      newAcc.setUser(user);
      oauthRepo.save(newAcc);
    }

    // 3. Generate Token & Redirect
    String accessToken = tokenService.generateToken(user, false);
    String refreshToken = tokenService.generateToken(user, true);

    addCookie(response, "accessToken", accessToken, 3600);
    addCookie(response, "refreshToken", refreshToken, 86400);

    response.sendRedirect(redirectUri);
  }

  private String extractFullName(OAuth2User oauthUser, String fallbackEmail) {
    String name = (String) oauthUser.getAttribute("name");
    if (name != null && !name.isBlank())
      return name;

    String givenName = (String) oauthUser.getAttribute("given_name");
    String familyName = (String) oauthUser.getAttribute("family_name");

    if (givenName != null && familyName != null)
      return givenName + " " + familyName;
    if (givenName != null)
      return givenName;
    if (familyName != null)
      return familyName;

    return fallbackEmail;
  }

  private User createNewUser(String email, String fullName, String avatarUrl, Provider provider) {
    Role userRole = roleRepo.findByName("USER")
        .orElseThrow(() -> new RuntimeException("Role USER not found"));

    User u = new User();
    u.setEmail(email);
    u.setProvider(provider);
    u.setEnabled(true);
    u.setFullName(fullName);
    u.setAvatarUrl(avatarUrl);
    u.setRegistrationDate(LocalDateTime.now());
    u.getRoles().add(userRole);
    return userRepo.save(u);
  }

  private User syncUserData(User user, String newName, String newAvatar) {
    boolean isModified = false;

    if (newName != null && !newName.isBlank() && !newName.equals(user.getFullName())) {
      user.setFullName(newName);
      isModified = true;
    }

    if (newAvatar != null && !newAvatar.isBlank() && !newAvatar.equals(user.getAvatarUrl())) {
      user.setAvatarUrl(newAvatar);
      isModified = true;
    }

    return isModified ? userRepo.save(user) : user;
  }

  private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(false);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    // cookie.setSecure(true); // Bật dòng này khi chạy HTTPS (Production)
    response.addCookie(cookie);
  }
}
