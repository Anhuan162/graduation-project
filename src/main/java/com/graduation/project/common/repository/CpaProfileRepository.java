package com.graduation.project.common.repository;

import com.graduation.project.common.entity.CpaProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpaProfileRepository extends JpaRepository<CpaProfile, UUID> {
  boolean existsByCpaProfileName(String cpaProfileName);
}
