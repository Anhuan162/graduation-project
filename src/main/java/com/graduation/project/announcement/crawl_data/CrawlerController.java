package com.graduation.project.announcement.crawl_data;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

  private final CrawlerService crawlerService;

  @PostMapping("/trigger")
  public ResponseEntity<String> triggerCrawl() {
    // Nên chạy async để không block thread của API
    new Thread(crawlerService::crawlAll).start();
    return ResponseEntity.ok("Đã kích hoạt tiến trình Crawl dữ liệu!");
  }
}
