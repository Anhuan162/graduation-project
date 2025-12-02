package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {

    @Query("SELECT a FROM Announcement a")
    Page<Announcement> getAllAnnouncement( Pageable pageable);
}
