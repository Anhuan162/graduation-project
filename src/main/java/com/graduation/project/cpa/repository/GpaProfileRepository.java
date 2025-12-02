package com.graduation.project.cpa.repository;

import com.graduation.project.cpa.entity.GpaProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GpaProfileRepository extends JpaRepository<GpaProfile, UUID> {}
