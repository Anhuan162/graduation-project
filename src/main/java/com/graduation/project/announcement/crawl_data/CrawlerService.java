package com.graduation.project.announcement.crawl_data;

import com.graduation.project.announcement.constant.AnnouncementProvider;
import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.dto.CrawlerSourceConfig;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.auth.repository.FileMetadataRepository;
import com.graduation.project.common.constant.AccessType;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

  private final AnnouncementRepository announcementRepository;
  private final FileMetadataRepository fileMetadataRepository;
  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36";

  // Giả sử bạn có service upload file (S3/Local)
  // private final FileStorageService fileStorageService;

  // 1. Cấu hình các nguồn crawl (Có thể đưa vào Database hoặc File config)
  public List<CrawlerSourceConfig> getConfigs() {
    List<CrawlerSourceConfig> configs = new ArrayList<>();

    // Cấu hình cho trang Giáo Vụ
    configs.add(
        CrawlerSourceConfig.builder()
            .name(AnnouncementProvider.GIAO_VU_PTIT)
            .listUrl("https://giaovu.ptit.edu.vn/thong-bao/thong-bao-tu-phong-giao-vu/")
            .nextPageSelector("a.next.page-numbers")
            .itemSelector(".posts-gv li .content") // Cần F12 để xem class thực tế
            .linkSelector(".post-title a")
            .titleSelector(".post-title")
            .contentSelector(".post-content")
            .subLinkSelector(".a")
            .build());

    // Cấu hình cho trang Portal chính (Thêm bao nhiêu tùy ý)
    configs.add(
        CrawlerSourceConfig.builder()
            .name(AnnouncementProvider.PORTAL_PTIT)
            .listUrl("https://ptit.edu.vn/tin-tuc-su-kien/thong-bao")
            .nextPageSelector("a.next.page-numbers")
            .itemSelector(".content")
            .linkSelector(".post-title a")
            .titleSelector(".post-title")
            .contentSelector(".post-content")
            .subLinkSelector(".a")
            .build());

    return configs;
  }

  // 2. Hàm chạy tự động (Scheduled)
  @Scheduled(cron = "${app.crawler.schedule.cron}", zone = "Asia/Ho_Chi_Minh")
  public void autoCrawl() {
    log.info("Bắt đầu tiến trình tự động crawl...");
    crawlAll();
  }

  public void crawlAll() {
    List<CrawlerSourceConfig> sources = getConfigs();
    for (CrawlerSourceConfig source : sources) {
      crawlSource(source);
    }
  }

  private void crawlSource(CrawlerSourceConfig config) {
    String currentUrl = config.getListUrl();
    boolean keepCrawling = true;
    int page = 1;

    while (currentUrl != null && keepCrawling && page <= 20) {
      try {
        log.info("Đang quét trang: " + currentUrl);
        Document doc = connectWithRetry(currentUrl);
        if (doc == null) {
          log.error("Không thể tải trang {} sau nhiều lần thử. Bỏ qua trang này.", currentUrl);
          // Quyết định: Dừng luôn hay thử trang sau? Thường nếu list lỗi thì nên dừng.
          keepCrawling = false;
          break;
        }

        Elements items = doc.select(config.getItemSelector());
        if (items.isEmpty()) {
          log.warn("Không tìm thấy bài viết nào với selector: " + config.getItemSelector());
          break;
        }

        boolean hasNewItemOnThisPage = false; // Kiểm tra trang này có bài mới không

        for (Element item : items) {
          // Lấy link chi tiết
          Element linkEl = item.select(config.getLinkSelector()).first();
          if (linkEl == null) continue;

          String detailUrl = linkEl.attr("abs:href");

          // [LOGIC QUAN TRỌNG NHẤT - STOP CONDITION]
          // Nếu URL đã tồn tại trong DB -> Đây là bài cũ
          if (announcementRepository.existsBySourceUrl(detailUrl)) {
            continue;
          }

          // Nếu chưa có -> Xử lý lưu
          processDetailPage(detailUrl, config);
          hasNewItemOnThisPage = true; // Đánh dấu là trang này có dữ liệu mới
        }

        // [QUYẾT ĐỊNH CÓ SANG TRANG TIẾP THEO KHÔNG?]
        // Nếu trang hiện tại KHÔNG CÓ bài mới nào (toàn bộ là bài cũ) -> DỪNG LẠI, không sang page
        // 2
        if (!hasNewItemOnThisPage) {
          log.info("Đã gặp toàn bộ bài cũ. Dừng crawl nguồn: " + config.getName());
          keepCrawling = false;
        } else {
          // Nếu vẫn có bài mới -> Tìm link trang tiếp theo để đi tiếp
          page++;
          Element nextBtn =
              doc.select(config.getNextPageSelector()).last(); // Thường nút Next ở cuối
          if (nextBtn != null) {
            currentUrl = nextBtn.attr("abs:href");
            Thread.sleep(1000); // Nghỉ 1s để tránh bị chặn IP
          } else {
            currentUrl = null; // Hết trang -> Dừng
          }
        }

      } catch (Exception e) {
        log.error("Lỗi khi crawl trang {}: {}", currentUrl, e.getMessage());
        keepCrawling = false; // Gặp lỗi thì dừng luôn cho an toàn
      }
    }
  }

  private Document connectWithRetry(String url) {
    int maxRetries = 3;
    int attempt = 0;

    while (attempt < maxRetries) {
      try {
        return Jsoup.connect(url)
            .userAgent(USER_AGENT) // Thêm User Agent
            .timeout(60000) // Tăng timeout lên 60 giây (QUAN TRỌNG)
            .get();
      } catch (SocketTimeoutException se) {
        attempt++;
        log.warn("Timeout khi kết nối {} (Lần {}/{}). Đang thử lại...", url, attempt, maxRetries);
        try {
          Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
      } catch (IOException e) {
        log.error("Lỗi IO khi kết nối {}: {}", url, e.getMessage());
        return null; // Lỗi 404, 500 thì không retry làm gì
      }
    }
    return null; // Hết số lần thử mà vẫn lỗi
  }

  protected void processDetailPage(String url, CrawlerSourceConfig config) throws IOException {
    Document doc = connectWithRetry(url);
    if (doc == null) return;

    String title = doc.select(config.getTitleSelector()).text();
    Element contentEl = doc.select(config.getContentSelector()).first();

    if (contentEl == null) return;

    String htmlContent = contentEl.html(); // Lấy HTML để hiển thị

    Announcement announcement = new Announcement();
    announcement.setTitle(title);
    announcement.setContent(htmlContent);
    announcement.setSourceUrl(url);
    announcement.setCreatedDate(LocalDate.now());
    announcement.setAnnouncementStatus(true);
    announcement.setAnnouncementType(AnnouncementType.ACADEMIC);
    announcement.setAnnouncementProvider(config.getName());

    Announcement savedAnnouncement = announcementRepository.save(announcement);

    Elements fileLinks =
        contentEl.select(
            "a[href$=.pdf], a[href$=.doc], a[href$=.docx], a[href$=.xls], a[href$=.xlsx]");

    for (Element fileLink : fileLinks) {
      String fileUrl = fileLink.attr("abs:href");
      String fileName = fileLink.text();

      // 3. Lưu FileMetadata
      FileMetadata metadata =
          FileMetadata.builder()
              .fileName(fileName)
              .url(fileUrl) // Đường dẫn file trên server của mình
              .resourceType(ResourceType.ANNOUNCEMENT) // Enum ResourceType
              .resourceId(savedAnnouncement.getId()) // FK mềm: UUID của Announcement
              .accessType(AccessType.PUBLIC)
              .size(0)
              .contentType("application/octet-stream")
              .build();

      fileMetadataRepository.save(metadata);
    }

    log.info("Đã lưu bài viết: " + title);
  }

  public String cleanTextForAI(String htmlContent) {
    if (htmlContent == null || htmlContent.isEmpty()) return "";

    // 1. Parse HTML
    Document doc = Jsoup.parse(htmlContent);

    // 2. Xóa các thẻ không cần thiết (Script, Style) để tránh rác
    doc.select("script, style, meta, iframe").remove();

    // 3. Xử lý xuống dòng: Thêm \n vào trước/sau các thẻ khối để tách đoạn
    // Khi Jsoup lấy text, nó sẽ giữ lại các \n này
    doc.select("br").append("\\n");
    doc.select("p, h1, h2, h3, h4, h5, h6, li, div").append("\\n");

    // 4. Lấy text và xử lý khoảng trắng thừa
    // doc.text() của Jsoup thông minh, nó sẽ trim bớt khoảng trắng
    String text = doc.text();

    // 5. Khôi phục lại \n thật (vì bước 3 ta append chuỗi literal "\\n")
    // Hoặc nếu bạn append("\n") ở bước 3 thì bước này không cần replace phức tạp
    // Nhưng cách an toàn nhất là dùng thư viện Whitelist (Safelist) của Jsoup:

    return Jsoup.clean(
        doc.html(), "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
  }
}
