package com.graduation.project.event.mapper;

import org.springframework.stereotype.Component;

import com.graduation.project.event.dto.ActivityLogResponse;
import com.graduation.project.event.entity.ActivityLog;

@Component
public class ActivityLogMapper {

    public ActivityLogResponse toResponse(ActivityLog log) {
        if (log == null) {
            return null;
        }
        ActivityLogResponse res = new ActivityLogResponse();
        res.setId(log.getId());

        if (log.getUser() != null) {
            res.setUserId(log.getUser().getId());
            res.setUsername(log.getUser().getFullName());
            res.setUserAvatar(log.getUser().getAvatarUrl());
        }

        res.setAction(log.getAction());
        res.setModule(log.getModule());
        res.setDescription(log.getDescription());
        res.setTargetId(log.getTargetId());
        res.setTargetType(log.getTargetType());
        res.setMetadata(log.getMetadata());
        res.setIpAddress(log.getIpAddress());
        res.setCreatedAt(log.getCreatedAt());

        return res;
    }
}
