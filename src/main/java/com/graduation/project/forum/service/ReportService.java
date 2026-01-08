package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ProcessReportRequest;
import com.graduation.project.forum.dto.ReportRequest;
import com.graduation.project.forum.dto.ReportResponse;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Report;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReportRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.graduation.project.common.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CurrentUserService currentUserService;
  private final AuthorizationService authorizationService;
  private final TopicRepository topicRepository;
  private final UserMapper userMapper;

  @Transactional
  public void createReport(ReportRequest request) {
    User reporter = currentUserService.getCurrentUserEntity();

    Report.ReportBuilder reportBuilder = Report.builder()
        .reporter(reporter)
        .reason(request.getReason())
        .description(request.getDescription())
        // .ipAddress(RequestUtils.getClientIpAddress())
        .status(ReportStatus.PENDING);

    if (request.getTargetType() == TargetType.POST) {
      handlePostReport(request.getTargetId(), reporter, reportBuilder);
    } else if (request.getTargetType() == TargetType.COMMENT) {
      handleCommentReport(request.getTargetId(), reporter, reportBuilder);
    } else if (request.getTargetType() == TargetType.TOPIC) {
      handleTopicReport(request.getTargetId(), reporter, reportBuilder);
    } else {
      throw new AppException(ErrorCode.INVALID_REPORT_TARGET);
    }

    reportRepository.save(reportBuilder.build());
  }

  private void handlePostReport(UUID postId, User reporter, Report.ReportBuilder builder) {
    Post post = postRepository
        .findById(postId)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    if (reportRepository.existsByPostIdAndReporterId(postId, reporter.getId())) {
      throw new AppException(ErrorCode.REPORT_ALREADY_EXISTED);
    }

    builder.targetType(TargetType.POST);
    builder.post(post);
  }

  private void handleCommentReport(UUID commentId, User reporter, Report.ReportBuilder builder) {
    Comment comment = commentRepository
        .findById(commentId)
        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

    if (reportRepository.existsByCommentIdAndReporterId(commentId, reporter.getId())) {
      throw new AppException(ErrorCode.REPORT_ALREADY_EXISTED);
    }

    builder.targetType(TargetType.COMMENT);
    builder.comment(comment);
  }

  private void handleTopicReport(UUID topicId, User reporter, Report.ReportBuilder builder) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    if (reportRepository.existsByTopicIdAndReporterId(topicId, reporter.getId())) {
      throw new AppException(ErrorCode.REPORT_ALREADY_EXISTED);
    }

    builder.targetType(TargetType.TOPIC);
    builder.topic(topic);
  }

  @Transactional(readOnly = true)
  public Page<ReportResponse> searchReportsForAdmin(
      ReportStatus status, TargetType type, Pageable pageable) {
    Page<Report> reports = reportRepository.findAllByFilters(status, type, pageable);
    return reports.map(this::mapToResponse);
  }

  @Transactional
  public Page<ReportResponse> searchReportsByTopic(UUID topicId, ReportStatus status, Pageable pageable) {
    User reporter = currentUserService.getCurrentUserEntity();
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    if (!authorizationService.canManageTopic(reporter, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    Page<Report> reports = reportRepository.findAllReportsByTopic(topicId, status, pageable);
    return reports.map(this::mapToResponse);
  }

  @Transactional(readOnly = true)
  public ReportResponse getReportDetail(UUID id) {
    Report report = reportRepository
        .findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
    return mapToResponse(report);
  }

  @Transactional
  public ReportResponse processReport(UUID reportId, ProcessReportRequest request) {
    Report report = reportRepository
        .findById(reportId)
        .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

    // Update Audit info
    report.setAdminNote(request.getNote());

    switch (request.getAction()) {
      case DELETE_CONTENT:
        report.setStatus(ReportStatus.RESOLVED);
        handleContentDeletion(report);
        break;

      case KEEP_CONTENT:
        // Giữ nội dung, có thể đánh dấu là REJECTED (Báo cáo sai) hoặc RESOLVED (Đã xem
        // xét nhưng ko xóa)
        // Ở đây chọn REJECTED để rõ ràng là báo cáo bị từ chối
        report.setStatus(ReportStatus.REJECTED);
        break;

      case WARN_USER:
        // TODO: Implement warn user logic
        report.setStatus(ReportStatus.RESOLVED);
        break;
    }

    reportRepository.save(report);
    return mapToResponse(report);
  }

  // Helper method: Xử lý xóa nội dung dựa trên TargetType
  private void handleContentDeletion(Report report) {
    if (report.getTargetType() == TargetType.POST && report.getPost() != null) {
      Post post = report.getPost();
      if (!post.getDeleted()) {
        post.setDeleted(true);
        postRepository.save(post);
      }
    } else if (report.getTargetType() == TargetType.COMMENT && report.getComment() != null) {
      Comment comment = report.getComment();
      if (!comment.getDeleted()) {
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
        .reporter(userMapper.toUserSummaryDto(report.getReporter()))
        .reason(report.getReason())
        .description(report.getDescription())
        .status(report.getStatus())
        .targetType(report.getTargetType())
        .postId(report.getTargetType() == TargetType.POST ? targetId : null)
        .commentId(report.getTargetType() == TargetType.COMMENT ? targetId : null)
        .topicId(report.getTargetType() == TargetType.TOPIC ? targetId : null)
        .targetPreview(targetPreview)
        .createdAt(report.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
        .build();
  }
}
