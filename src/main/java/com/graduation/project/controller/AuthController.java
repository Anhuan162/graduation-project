package com.graduation.project.controller;

import com.graduation.project.entity.Provider;
import com.graduation.project.entity.Role;
import com.graduation.project.entity.User;
import com.graduation.project.repository.UserRepository;
import com.graduation.project.security.*;
import com.graduation.project.service.CustomUserDetailsService;
import com.graduation.project.service.RefreshTokenService;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authManager,
                          UserRepository userRepo,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils,
                          RefreshTokenService refreshTokenService,
                          CustomUserDetailsService userDetailsService) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        String raw = body.get("password");
        if (userRepo.findByEmail(email).isPresent()) return ResponseEntity.badRequest().body("Email exists");
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(raw));
        u.setProvider(Provider.LOCAL);
        // assign default role - here simplified
        userRepo.save(u);
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        CustomUserDetails ud = (CustomUserDetails) auth.getPrincipal();
        User user = userRepo.findByEmail(email).orElseThrow();
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String access = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), roles);
        String refresh = jwtUtils.generateRefreshToken(user.getEmail());
        refreshTokenService.createRefreshTokenForUser(user, refresh);
        Map<String,String> res = Map.of("accessToken", access, "refreshToken", refresh);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> body) {
        String refreshToken = body.get("refreshToken");
        if (!jwtUtils.validate(refreshToken)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String email = jwtUtils.getUsername(refreshToken);
        if (!refreshTokenService.validateRefreshToken(refreshToken)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepo.findByEmail(email).orElseThrow();
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();
        String newAccess = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), roles);
        return ResponseEntity.ok(Map.of("accessToken", newAccess));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String,String> body) {
        String refreshToken = body.get("refreshToken");
        refreshTokenService.revokeRefreshToken(refreshToken);
        return ResponseEntity.ok("logged out");
    }
}
