package com.graduation.project.announcement.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementFileResponse {
    private UUID id;
    private String fileName;
    private String url;
    private String fileType;
    private long size;
}
