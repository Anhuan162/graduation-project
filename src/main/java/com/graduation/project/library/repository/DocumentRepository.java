package com.graduation.project.library.repository;

import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.entity.Document;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

  @Query("SELECT d "
      + " from Document as d where  "
      + " (:title is NULL or d.title like concat('%', :title, '%')) and "
      + " (:documentType is null or :documentType = d.documentType ) and  "
      + " (:documentStatus is null or :documentStatus = d.documentStatus ) and  "
      + " (:subjectId is null or d.subject.id = :subjectId ) and "
      + " (:uploaderId is null or d.uploadedBy.id = :uploaderId)")
  Page<Document> findByTitleAndDocumentTypeAndSubjectIdAndDocumentStatus(
      @Param("title") String title,
      @Param("documentType") DocumentType documentType,
      @Param("subjectId") UUID subjectId,
      @Param("documentStatus") com.graduation.project.library.constant.DocumentStatus documentStatus,
      @Param("uploaderId") UUID uploaderId,
      Pageable pageable);

  List<Document> findByTitle(String title);

  // CRITICAL: Must use LOWER() to match database index
  // uk_document_title_subject_ci
  // Spring's IgnoreCase generates UPPER() which doesn't use the index!
  @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Document d WHERE LOWER(d.title) = LOWER(:title) AND d.subject.id = :subjectId")
  boolean existsByTitleCaseInsensitiveAndSubjectId(@Param("title") String title, @Param("subjectId") UUID subjectId);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "subject", "uploadedBy" })
  Page<Document> findByUploadedBy_Id(UUID uploadedById, Pageable pageable);

  long countByUploadedBy_IdAndDocumentStatus(UUID uploadedById,
      com.graduation.project.library.constant.DocumentStatus documentStatus);
}
