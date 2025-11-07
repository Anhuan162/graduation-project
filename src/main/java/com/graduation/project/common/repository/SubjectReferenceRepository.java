package com.graduation.project.common.repository;

import com.graduation.project.common.entity.SubjectReference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectReferenceRepository extends JpaRepository<SubjectReference, UUID> {}
