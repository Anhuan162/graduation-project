package com.graduation.project.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TopicInfoResponse {
    private UUID id;
    private String name;
    private String slug;
}
