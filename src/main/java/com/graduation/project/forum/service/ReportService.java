package com.graduation.project.forum.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Report;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReportRepository;
import com.graduation.project.forum.dto.ReportRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CurrentUserService currentUserService;

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
}
