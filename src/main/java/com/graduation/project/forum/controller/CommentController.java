package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.CommentRequest;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/post/{postId}")
  public ApiResponse<CommentResponse> createComment(
      @PathVariable UUID postId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.createComment(postId, request))
        .build();
  }

  @GetMapping("/post/{postId}")
  public ApiResponse<Page<CommentResponse>> getRootComments(
      @PathVariable UUID postId,
      Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getRootComments(postId, pageable))
        .build();
  }

  @GetMapping("/replies/{rootCommentId}")
  public ApiResponse<Page<CommentResponse>> getReplies(
      @PathVariable UUID rootCommentId,
      @PageableDefault(page = 0, size = 10, sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getReplies(rootCommentId, pageable))
        .build();
  }

  @PutMapping("/{commentId}")
  public ApiResponse<CommentResponse> updateComment(
      @PathVariable UUID commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.updateComment(commentId, request))
        .build();
  }

  @DeleteMapping("/{commentId}")
  public ApiResponse<String> softDeleteComment(@PathVariable UUID commentId) {
    commentService.softDeleteComment(commentId);
    return ApiResponse.<String>builder()
        .result("Comment deleted successfully")
        .build();
  }

  @GetMapping("/my-comments")
  public ApiResponse<Page<CommentResponse>> getMyComments(Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getMyComments(pageable))
        .build();
  }
}
