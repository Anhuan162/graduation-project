package com.graduation.project.common.repository;

import com.graduation.project.common.entity.Comment;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  Page<Comment> findByPostIdAndParentIsNull(UUID postId, Pageable pageable);

  Page<Comment> findByParentId(UUID id, Pageable pageable);

  long countByParentId(UUID id);
}
