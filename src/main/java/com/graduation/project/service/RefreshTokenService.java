package com.graduation.project.service;

import com.graduation.project.entity.RefreshToken;
import com.graduation.project.entity.User;
import com.graduation.project.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    public RefreshTokenService(RefreshTokenRepository repo) { this.repo = repo; }

    public void createRefreshTokenForUser(User user, String token) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUser(user);
        rt.setExpiryDate(LocalDateTime.now().plusDays(7));
        rt.setRevoked(false);
        repo.save(rt);
    }

    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> r = repo.findByToken(token);
        return r.isPresent() && !r.get().getRevoked() && r.get().getExpiryDate().isAfter(LocalDateTime.now());
    }

    public void revokeRefreshToken(String token) {
        repo.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }
}
