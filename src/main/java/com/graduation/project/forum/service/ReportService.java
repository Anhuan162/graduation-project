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

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CurrentUserService currentUserService;
  private final AuthorizationService authorizationService;
  private final TopicRepository topicRepository;

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

  @Transactional(readOnly = true)
  public Page<ReportResponse> searchReportsForAdmin(
      ReportStatus status, TargetType type, Pageable pageable) {
    Page<Report> reports = reportRepository.findAllByFilters(status, type, pageable);
    return reports.map(this::mapToResponse);
  }

  @Transactional
  public Page<ReportResponse> searchReportsByTopic(UUID topicId, Pageable pageable) {
    User reporter = currentUserService.getCurrentUserEntity();
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    if (!authorizationService.canManageTopic(reporter, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    Page<Report> reports = reportRepository.findAllReportsByTopic(topicId, pageable);
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

    report.setStatus(request.getStatus());

    if (request.isDeleteTarget() && request.getStatus() == ReportStatus.RESOLVED) {
      handleContentDeletion(report);
    }

    reportRepository.save(report);
    return mapToResponse(report);
  }

  private void handleContentDeletion(Report report) {
    if (report.getTargetType() == TargetType.POST && report.getPost() != null) {
      Post post = report.getPost();
      if (!post.isDeleted()) {
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

  private ReportResponse mapToResponse(Report report) {
    String targetPreview = "Content deleted or unavailable";
    UUID targetId = null;

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
        .reporterId(report.getReporter().getId())
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
