package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.Announcement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository
        extends JpaRepository<Announcement, UUID>, JpaSpecificationExecutor<Announcement> {
}
