package com.graduation.project.forum.dto;

import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackViewResponse {
    private UUID postId;
    private Long viewCount;
    private Boolean counted;
}
