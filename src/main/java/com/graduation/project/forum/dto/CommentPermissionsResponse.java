package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentPermissionsResponse {
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canReport;
}
