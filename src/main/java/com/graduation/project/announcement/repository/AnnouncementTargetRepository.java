package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.AnnouncementTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnnouncementTargetRepository extends JpaRepository<AnnouncementTarget, UUID> {
}
