package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.TopicRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTopicMemberRoleRequest {
    @NotNull(message = "topicRole is required")
    private TopicRole topicRole;
}
