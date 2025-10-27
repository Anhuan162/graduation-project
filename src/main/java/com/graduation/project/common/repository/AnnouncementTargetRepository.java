package com.graduation.project.common.repository;

import com.graduation.project.common.entity.AnnouncementTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementTargetRepository extends JpaRepository<AnnouncementTarget, String> {}
