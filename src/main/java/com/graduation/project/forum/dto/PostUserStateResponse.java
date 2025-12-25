package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUserStateResponse {
    private Boolean isLiked;
    private Boolean isSaved;
    private Boolean isFollowing;
}
