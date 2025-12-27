package com.graduation.project.forum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUserStateResponse {
    private Boolean liked;
    private Boolean saved;
    private Boolean following;
}
