package com.graduation.project.forum.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostStatDTO {
  private String date;
  private Long count;

  // SỬA: Tham số đầu tiên để là Object
  public PostStatDTO(Object date, Long count) {
    // Ép kiểu an toàn sang String
    this.date = date != null ? date.toString() : null;
    this.count = count;
  }
}
