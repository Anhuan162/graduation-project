package com.graduation.project.announcement.specification;

import com.graduation.project.announcement.entity.Announcement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AnnouncementSpecification {

    public static Specification<Announcement> withType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.trim().isEmpty())
                return null;
            return cb.equal(root.get("announcementType"), type.trim());
        };
    }

    public static Specification<Announcement> withStatus(Boolean status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("announcementStatus"), status);
        };
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
        return (root, query, cb) -> {
            if (fromDate == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdDate"), fromDate);
        };
    }

    public static Specification<Announcement> toDate(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdDate"), toDate);
        };
    }
}
