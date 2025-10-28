CREATE TABLE `users`
(
    `id`           CHAR(36) NOT NULL,
    `avatar_url`   varchar(255) DEFAULT NULL,
    `email`        varchar(255) DEFAULT NULL,
    `enabled`      bit(1)       DEFAULT NULL,
    `full_name`    varchar(255) DEFAULT NULL,
    `password`     varchar(255) DEFAULT NULL,
    `phone`        varchar(255) DEFAULT NULL,
    `provider`     enum('FACEBOOK','GOOGLE','LOCAL') NOT NULL,
    `student_code` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `announcements`
(
    `id`                  CHAR(36) NOT NULL,
    `announcement_status` bit(1)       DEFAULT NULL,
    `announcement_type`   enum('CLASS_MEETING','PAY_FEE') DEFAULT NULL,
    `content`             varchar(255) DEFAULT NULL,
    `created_date`        date         DEFAULT NULL,
    `modified_date`       date         DEFAULT NULL,
    `title`               varchar(255) DEFAULT NULL,
    `created_by`          CHAR(36) DEFAULT NULL,
    `modified_by`         CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                   `FKht7cvemps7a8tjylacwtyyckj` (`created_by`),
    KEY                   `FKsjuhu87st1r2l90sisggru5xc` (`modified_by`),
    CONSTRAINT `FKht7cvemps7a8tjylacwtyyckj` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `FKsjuhu87st1r2l90sisggru5xc` FOREIGN KEY (`modified_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `audit_log`
(
    `id`         CHAR(36) NOT NULL,
    `action`     varchar(255) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `ip_address` varchar(255) DEFAULT NULL,
    `user_id`    CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKk4alalwu62gj4tfbgfefll3tu` (`user_id`),
    CONSTRAINT `FKk4alalwu62gj4tfbgfefll3tu` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `faculties`
(
    `id`           CHAR(36) NOT NULL,
    `description`  varchar(255) DEFAULT NULL,
    `faculty_code` varchar(255) DEFAULT NULL,
    `faculty_name` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `classrooms`
(
    `id`               CHAR(36) NOT NULL,
    `class_code`       varchar(255) DEFAULT NULL,
    `class_name`       varchar(255) DEFAULT NULL,
    `ended_year`       int          DEFAULT NULL,
    `school_year_code` varchar(255) DEFAULT NULL,
    `started_year`     int          DEFAULT NULL,
    `faculty_id`       CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                `FKhv44pa95g81nv36f1m8lpny9d` (`faculty_id`),
    CONSTRAINT `FKhv44pa95g81nv36f1m8lpny9d` FOREIGN KEY (`faculty_id`) REFERENCES `faculties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `announcement_targets`
(
    `id`              CHAR(36) NOT NULL,
    `classroom_code`  varchar(255) DEFAULT NULL,
    `announcement_id` CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY               `FK6i2lcfqud13ksff1q1qxt7vn2` (`announcement_id`),
    CONSTRAINT `FK6i2lcfqud13ksff1q1qxt7vn2` FOREIGN KEY (`announcement_id`) REFERENCES `announcements` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `invalidated_token`
(
    `id`          CHAR(36) NOT NULL,
    `expiry_time` datetime(6) DEFAULT NULL,
    `issued_at`   datetime(6) DEFAULT NULL,
    `jit`         varchar(255) DEFAULT NULL,
    `user_id`     CHAR(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY           `FKpv7k1rtxl6wjqu8o05oachfp1` (`user_id`),
    CONSTRAINT `FKpv7k1rtxl6wjqu8o05oachfp1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_events`
(
    `id`         CHAR(36) NOT NULL,
    `content`    text,
    `created_at` datetime(6) DEFAULT NULL,
    `related_id` CHAR(36) DEFAULT NULL,
    `title`      varchar(255) DEFAULT NULL,
    `type`       enum('ANNOUNCEMENT','COMMENT','POST') DEFAULT NULL,
    `created_by` CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKo8x1wf7aqakauhj8iorlf9iyn` (`created_by`),
    CONSTRAINT `FKo8x1wf7aqakauhj8iorlf9iyn` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_notifications`
(
    `id`                    CHAR(36) NOT NULL,
    `delivered_at`          datetime(6) DEFAULT NULL,
    `is_read`               bit(1) NOT NULL,
    `notification_status`   enum('DELETED','READ','SENT','UNREAD') DEFAULT NULL,
    `read_at`               datetime(6) DEFAULT NULL,
    `notification_event_id` CHAR(36) DEFAULT NULL,
    `user_id`               CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                     `FK9587ifdkk502aiwxb8tk5rrt2` (`notification_event_id`),
    KEY                     `FK9f86wonnl11hos1cuf5fibutl` (`user_id`),
    CONSTRAINT `FK9587ifdkk502aiwxb8tk5rrt2` FOREIGN KEY (`notification_event_id`) REFERENCES `notification_events` (`id`),
    CONSTRAINT `FK9f86wonnl11hos1cuf5fibutl` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `oauth_accounts`
(
    `id`               CHAR(36)       NOT NULL,
    `access_token`     varchar(255) DEFAULT NULL,
    `expires_at`       datetime(6) DEFAULT NULL,
    `provider`         enum('FACEBOOK','GOOGLE','LOCAL') DEFAULT NULL,
    `provider_user_id` varchar(255) NOT NULL,
    `refresh_token`    varchar(255) DEFAULT NULL,
    `user_id`          CHAR(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY                `FKn6j2b3ur1fcj3vkodrmqtdxmi` (`user_id`),
    CONSTRAINT `FKn6j2b3ur1fcj3vkodrmqtdxmi` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `roles`
(
    `name`        varchar(255) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `permissions`
(
    `name`            varchar(255) NOT NULL,
    `permission_type` enum('create_all','create_any','delete_all','delete_any','read_all','read_any','update_all','update_any') DEFAULT NULL,
    `resource_type`   enum('ANNOUNCEMENT','CLASSROOM','FACULTY','NOTIFICATION_EVENT','PERMISSION','ROLE','USER') DEFAULT NULL,
    PRIMARY KEY (`name`),
    UNIQUE KEY `UKhqg1lom4f2bv8tqincp1xdb38` (`resource_type`,`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `roles_permissions`
(
    `role_name`        varchar(255) NOT NULL,
    `permissions_name` varchar(255) NOT NULL,
    PRIMARY KEY (`role_name`, `permissions_name`),
    KEY                `FK9u1xpvjxbdnkca024o6fyr7uu` (`permissions_name`),
    CONSTRAINT `FK6nw4jrj1tuu04j9rk7xwfssd6` FOREIGN KEY (`role_name`) REFERENCES `roles` (`name`),
    CONSTRAINT `FK9u1xpvjxbdnkca024o6fyr7uu` FOREIGN KEY (`permissions_name`) REFERENCES `permissions` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `user_roles`
(
    `user_id` CHAR(36) NOT NULL,
    `role_id` varchar(255) NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY       `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
    CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`name`),
    CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `verification_tokens`
(
    `id`          CHAR(36) NOT NULL,
    `expiry_date` datetime(6) DEFAULT NULL,
    `token`       varchar(255) DEFAULT NULL,
    `user_id`     CHAR(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKdqp95ggn6gvm865km5muba2o5` (`user_id`),
    CONSTRAINT `FK54y8mqsnq1rtyf581sfmrbp4f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



