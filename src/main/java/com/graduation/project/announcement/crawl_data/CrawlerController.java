package com.graduation.project.announcement.crawl_data;

import com.graduation.project.auth.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

  private final CrawlerService crawlerService;

  @PostMapping("/trigger")
  public ApiResponse<String> triggerCrawl() {
    // Nên chạy async để không block thread của API
    new Thread(crawlerService::crawlAll).start();
    return ApiResponse.<String>builder().result("Đã kích hoạt tiến trình Crawl dữ liệu!").build();
  }

  @PostMapping("/trigger-subjects")
  public ApiResponse<String> triggerCrawlSubjects() {
    // Nên chạy async để không block thread của API
    new Thread(crawlerService::crawlAllSubjects).start();
    return ApiResponse.<String>builder().result("Đã kích hoạt tiến trình Crawl dữ liệu!").build();
  }
}
