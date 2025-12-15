package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.entity.DomainEvent;
import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ProcessReportRequest;
import com.graduation.project.forum.dto.ReportRequest;
import com.graduation.project.forum.dto.ReportResponse;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Report;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReportRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher publisher;

  @Transactional
  public void createReport(ReportRequest request) {
    User reporter = currentUserService.getCurrentUserEntity();

    Report.ReportBuilder reportBuilder =
        Report.builder()
            .reporter(reporter)
            .reason(request.getReason())
            .description(request.getDescription())
            //                .ipAddress(RequestUtils.getClientIpAddress())
            .status(ReportStatus.PENDING);

    if (request.getTargetType() == TargetType.POST) {
      handlePostReport(request.getTargetId(), reporter, reportBuilder);
    } else if (request.getTargetType() == TargetType.COMMENT) {
      handleCommentReport(request.getTargetId(), reporter, reportBuilder);
    } else {
      throw new AppException(ErrorCode.INVALID_REPORT_TARGET);
    }

    DomainEvent event =
        DomainEvent.builder()
            .actorId(reporter.getId())
            .actorName(reporter.getEmail()) // hoặc fullName
            .action("CREATE_REPORT") // Nên dùng Enum
            .module("FORUM") // Nên dùng Enum
            .resourceId(
                reportBuilder
                    .build()
                    .getId()) // ID của cái Report vừa tạo (lưu ý: cần save trước hoặc lấy ID sau
            // save)
            .resourceType(ResourceType.REPORT)
            .title("Báo cáo vi phạm mới")
            .content("Người dùng " + reporter.getEmail() + " đã báo cáo một nội dung.")
//            .recipientIds(getAdminIds()) // Logic lấy ID admin để thông báo
            .localDateTime(LocalDateTime.now())
            .build();
    publisher.publishEvent(event);

    reportRepository.save(reportBuilder.build());
  }

  private void handlePostReport(UUID postId, User reporter, Report.ReportBuilder builder) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    if (reportRepository.existsByPostIdAndReporterId(postId, reporter.getId())) {
      throw new AppException(ErrorCode.REPORT_ALREADY_EXISTED);
    }

    builder.targetType(TargetType.POST);
    builder.post(post);
  }

  private void handleCommentReport(UUID commentId, User reporter, Report.ReportBuilder builder) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    if (reportRepository.existsByCommentIdAndReporterId(commentId, reporter.getId())) {
      throw new AppException(ErrorCode.REPORT_ALREADY_EXISTED);
    }

    builder.targetType(TargetType.COMMENT);
    builder.comment(comment);
  }

  @Transactional(readOnly = true)
  public Page<ReportResponse> getReports(ReportStatus status, TargetType type, Pageable pageable) {
    // Cần viết thêm method query trong Repository (xem bước 4)
    Page<Report> reports = reportRepository.findAllByFilters(status, type, pageable);
    return reports.map(this::mapToResponse);
  }

  @Transactional(readOnly = true)
  public ReportResponse getReportDetail(UUID id) {
    Report report =
        reportRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
    return mapToResponse(report);
  }

  @Transactional
  public void processReport(UUID reportId, ProcessReportRequest request) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

    // 1. Cập nhật trạng thái báo cáo
    report.setStatus(request.getStatus());

    // 2. Logic xử lý nội dung vi phạm (Nếu Admin chọn xóa)
    if (request.isDeleteTarget() && request.getStatus() == ReportStatus.RESOLVED) {
      handleContentDeletion(report);
    }

    reportRepository.save(report);
  }

  // Helper method: Xử lý xóa nội dung dựa trên TargetType
  private void handleContentDeletion(Report report) {
    if (report.getTargetType() == TargetType.POST && report.getPost() != null) {
      Post post = report.getPost();
      if (!post.getDeleted()) { // Chỉ xóa nếu chưa xóa
        post.setDeleted(true);
        postRepository.save(post);
      }
    } else if (report.getTargetType() == TargetType.COMMENT && report.getComment() != null) {
      Comment comment = report.getComment();
      if (!comment.isDeleted()) {
        comment.setDeleted(true);
        commentRepository.save(comment);
      }
    }
  }

  // Helper method: Mapper từ Entity sang DTO
  private ReportResponse mapToResponse(Report report) {
    String targetPreview = "Content deleted or unavailable";
    UUID targetId = null;

    // Lấy trích dẫn nội dung để hiển thị
    if (report.getTargetType() == TargetType.POST && report.getPost() != null) {
      targetId = report.getPost().getId();
      String content = report.getPost().getContent();
      targetPreview = content != null ? content.substring(0, Math.min(content.length(), 100)) : "";
    } else if (report.getTargetType() == TargetType.COMMENT && report.getComment() != null) {
      targetId = report.getComment().getId();
      String content = report.getComment().getContent();
      targetPreview = content != null ? content.substring(0, Math.min(content.length(), 100)) : "";
    }

    return ReportResponse.builder()
        .id(report.getId())
        .reporterId(report.getReporter().getId()) // Hoặc getFullName
        .reason(report.getReason())
        .description(report.getDescription())
        .status(report.getStatus())
        .targetType(report.getTargetType())
        .postId(report.getTargetType() == TargetType.POST ? targetId : null)
        .commentId(report.getTargetType() == TargetType.COMMENT ? targetId : null)
        .targetPreview(targetPreview)
        .createdAt(report.getCreatedAt())
        .build();
  }
}
