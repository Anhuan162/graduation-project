package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.InvalidatedToken;
import com.graduation.project.common.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, UUID> {
    boolean existsByJit(String jit);

    List<InvalidatedToken> findByUser(User user);

    int deleteByExpiryTimeBefore(java.util.Date expiryTime);
}
