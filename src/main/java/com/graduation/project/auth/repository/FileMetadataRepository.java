package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.FileMetadata;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
  List<FileMetadata> findAllByUserId(UUID userId);
}
