package com.graduation.project.common.dto;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSummaryDto {
    UUID id;
    String fullName;
    String avatarUrl;
    String studentCode;
    String classCode;
    String email;
}
