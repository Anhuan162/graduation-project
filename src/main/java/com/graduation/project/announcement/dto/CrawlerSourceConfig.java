package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.constant.AnnouncementProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerSourceConfig {
  private AnnouncementProvider name; // Tên nguồn (VD: Giáo vụ PTIT)
  private String listUrl; // Link trang danh sách (VD: https://giaovu.ptit.edu.vn/...)
  private String nextPageSelector;

  // CSS Selectors (Dùng để Jsoup biết lấy dữ liệu ở đâu)
  private String itemSelector; // Class bao quanh bài viết ở trang danh sách
  private String linkSelector; // Thẻ a chứa link chi tiết
  private String titleSelector; // Class chứa tiêu đề (ở trang chi tiết)
  private String contentSelector; // Class chứa nội dung (ở trang chi tiết)
  private String dateSelector; // Class chứa ngày đăng (ở trang chi tiết)

  private String subLinkSelector; // Thẻ a chứa link chi tiết
}
