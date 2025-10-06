package com.graduation.project.security;

import com.graduation.project.entity.*;
import com.graduation.project.repository.*;
import com.graduation.project.service.RefreshTokenService;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final OauthAccountRepository oauthRepo;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshService;
    private final String redirectUri; // from properties

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepo,
                                              OauthAccountRepository oauthRepo,
                                              JwtUtils jwtUtils,
                                              RefreshTokenService refreshService,
                                              @Value("${app.oauth2.authorizedRedirectUri}") String redirectUri) {
        this.userRepo = userRepo;
        this.oauthRepo = oauthRepo;
        this.jwtUtils = jwtUtils;
        this.refreshService = refreshService;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String provider = authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("OAUTH").toUpperCase();
        // Better: get registrationId from OAuth2AuthenticationToken cast, but kept simple here

        String email = (String) oauthUser.getAttributes().get("email");
        String providerUserId = (String) oauthUser.getAttribute("sub"); // google uses "sub"; facebook uses "id"

        // Try find oauth account
        Optional<OauthAccount> accOpt = oauthRepo.findByProviderAndProviderUserId(Provider.valueOf(provider), providerUserId);
        User user;
        if (accOpt.isPresent()) {
            user = accOpt.get().getUser();
        } else {
            // Try find user by email
            user = userRepo.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setProvider(Provider.GOOGLE); // or FACEBOOK depending
                u.setEnabled(true);
                u.setFullName((String) oauthUser.getAttribute("name"));
                // assign default USER role — assume role exists in DB
                Role role = new Role(); role.setName("USER");
                u.getRoles().add(role);
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

        // redirect to front-end with tokens as params (or store in cookie)
        String target = String.format("%s?accessToken=%s&refreshToken=%s", redirectUri, accessToken, refreshToken);
        response.sendRedirect(target);
    }
}
