package com.graduation.project.common.repository;

import com.graduation.project.common.entity.GradeSubjectAverageProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeSubjectAverageProfileRepository
    extends JpaRepository<GradeSubjectAverageProfile, UUID> {}
