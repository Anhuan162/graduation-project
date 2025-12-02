package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.InvalidatedToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, UUID> {
    boolean existsByJit(String jit);
}
