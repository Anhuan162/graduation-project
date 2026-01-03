package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.PostStatus;

import jakarta.validation.constraints.NotNull;

public class UpdatePostStatusRequest {
    @NotNull
    private PostStatus status;

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }
}
