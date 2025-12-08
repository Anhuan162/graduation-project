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

  @Query(
      "SELECT d "
          + " from Document as d where  "
          + " (:title is NULL or d.title like concat('%', :title, '%')) and "
          + " (:documentType is null or :documentType = d.documentType ) and  "
          + " (:subjectId is null or d.subject.id = :subjectId )")
  Page<Document> findByTitleAndDocumentTypeAndSubjectId(
      @Param("title") String title,
      @Param("documentType") DocumentType documentType,
      @Param("subjectId") UUID subjectId,
      Pageable pageable);

  List<Document> findByTitle(String title);
}
