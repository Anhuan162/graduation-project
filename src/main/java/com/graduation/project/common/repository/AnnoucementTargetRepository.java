package com.graduation.project.common.repository;

import com.graduation.project.common.entity.AnnoucementTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnoucementTargetRepository extends JpaRepository<AnnoucementTarget, String> {}
