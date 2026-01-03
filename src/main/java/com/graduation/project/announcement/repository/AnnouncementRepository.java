package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.Announcement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.NonNull;

@Repository
public interface AnnouncementRepository
                extends JpaRepository<Announcement, UUID>, JpaSpecificationExecutor<Announcement> {

        @Override
        @NonNull
        @EntityGraph(attributePaths = { "createdBy" })
        Page<Announcement> findAll(Specification<Announcement> spec, @NonNull Pageable pageable);
}
