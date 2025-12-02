package com.graduation.project.auth.repository;

import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.constant.ResourceType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
  List<FileMetadata> findAllByUserId(UUID userId);

  List<FileMetadata> findAllByIdIn(List<UUID> fileMetadataIds);

  List<FileMetadata> findAllByResourceIdAndResourceType(UUID resourceId, ResourceType resourceType);

  List<FileMetadata> findAllByResourceIdInAndResourceType(
      List<UUID> commentIds, ResourceType resourceType);
}
