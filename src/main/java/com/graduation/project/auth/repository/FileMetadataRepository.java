package com.graduation.project.auth.repository;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.dto.SearchFileRequest;
import com.graduation.project.common.entity.FileMetadata;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
  List<FileMetadata> findAllByUserId(UUID userId);

  List<FileMetadata> findAllByIdIn(List<UUID> fileMetadataIds);

  List<FileMetadata> findAllByResourceIdAndResourceType(UUID resourceId, ResourceType resourceType);

  List<FileMetadata> findAllByResourceIdInAndResourceType(
      List<UUID> commentIds, ResourceType resourceType);

  List<FileMetadata> findByResourceTypeAndResourceIdIn(
      ResourceType resourceType, List<UUID> postIds);

  // Trong FileMetadataRepository
  // Cũ: List<FileMetadata> findAllByResourceIdAndResourceType(UUID resourceId, ResourceType
  // resourceType);

  // Mới: Trả về Optional
  Optional<FileMetadata> findByResourceIdAndResourceType(
      UUID resourceId, ResourceType resourceType);

  @Query(
      "SELECT f FROM FileMetadata f "
          + "WHERE (:#{#req.folder} IS NULL OR f.folder = :#{#req.folder}) "
          + "AND (:#{#req.resourceType} IS NULL OR f.resourceType = :#{#req.resourceType}) "
          + "AND (:#{#req.resourceId} IS NULL OR f.resourceId = :#{#req.resourceId}) "
          + "AND (:#{#req.fromDate} IS NULL OR f.createdAt >= :#{#req.fromDate}) "
          + "AND (:#{#req.toDate} IS NULL OR f.createdAt <= :#{#req.toDate})")
  Page<FileMetadata> searchFiles(
      @Param("req") SearchFileRequest searchFileRequest, Pageable pageable);
}
