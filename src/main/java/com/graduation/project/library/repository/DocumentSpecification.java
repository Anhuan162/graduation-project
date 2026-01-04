package com.graduation.project.library.repository;

import com.graduation.project.library.constant.DocumentStatus;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.entity.Document;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DocumentSpecification {

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("documentStatus"), status);
        };
    }

    public static Specification<Document> containsTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(title)) {
                return criteriaBuilder.conjunction();
            }
            // ILIKE implementation varies, manually lowering is safer for cross-DB
            // compatibility often
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Document> hasSubjectId(UUID subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("subject").get("id"), subjectId);
        };
    }

    public static Specification<Document> hasType(DocumentType documentType) {
        return (root, query, criteriaBuilder) -> {
            if (documentType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("documentType"), documentType);
        };
    }

    public static Specification<Document> createdBy(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction(); // Or disjunction if strict? Usually conjunction to ignore
            }
            return criteriaBuilder.equal(root.get("uploadedBy").get("id"), userId);
        };
    }

    public static Specification<Document> isPremium(Boolean isPremium) {
        return (root, query, criteriaBuilder) -> {
            if (isPremium == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isPremium"), isPremium);
        };
    }
}
