package com.graduation.project.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {
  private String fileId;
  private String fileName;
  private String webContentLink; // Link tải xuống
  private String webViewLink; // Link xem trực tiếp (preview)
  private String type;
}
