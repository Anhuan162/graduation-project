package com.graduation.project.cpa.repository;

import com.graduation.project.cpa.entity.CpaProfile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpaProfileRepository extends JpaRepository<CpaProfile, UUID> {
  boolean existsByCpaProfileName(String cpaProfileName);

  List<CpaProfile> findAllByUserId(UUID id);
}
