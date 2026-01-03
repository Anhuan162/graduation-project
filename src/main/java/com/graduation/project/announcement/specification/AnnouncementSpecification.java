package com.graduation.project.announcement.specification;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.entity.AnnouncementTarget;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class AnnouncementSpecification {

    private AnnouncementSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<Announcement> withType(AnnouncementType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("announcementType"), type);
    }

    public static Specification<Announcement> withStatus(Boolean status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("announcementStatus"), status);
    }

    public static Specification<Announcement> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return null;
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("content")), like));
        };
    }

    public static Specification<Announcement> fromDate(LocalDate fromDate) {
        return (root, query, cb) -> fromDate == null ? null
                : cb.greaterThanOrEqualTo(root.get("createdDate"), fromDate);
    }

    public static Specification<Announcement> toDate(LocalDate toDate) {
        return (root, query, cb) -> toDate == null ? null
                : cb.lessThanOrEqualTo(root.get("createdDate"), toDate);
    }

    public static Specification<Announcement> withClassroomCode(String classroomCode) {
        return (root, query, cb) -> {
            // Global announcements have NO targets (empty list)
            // Specific announcements have targets that match classroomCode
            query.distinct(true);

            Join<Announcement, AnnouncementTarget> targetJoin = root.join("targets", JoinType.LEFT);

            // Condition 1: Global announcement (targetJoin.id is null)
            // Condition 2: Matches classroomCode (if classroomCode is present)

            if (classroomCode == null || classroomCode.trim().isEmpty()) {
                // If user has no class code, they can ONLY see global announcements
                return cb.isNull(targetJoin.get("id"));
            }

            return cb.or(
                    cb.isNull(targetJoin.get("id")),
                    cb.equal(cb.upper(targetJoin.get("classroomCode")), classroomCode.trim().toUpperCase()));
        };
    }
}
