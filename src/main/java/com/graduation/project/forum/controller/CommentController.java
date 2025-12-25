package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.CommentRequest;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/post/{postId}")
  public ApiResponse<CommentResponse> createRootComment(
      @PathVariable String postId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.ok(commentService.createRootComment(postId, request));
  }

  @PostMapping("/reply/{commentId}")
  public ApiResponse<CommentResponse> replyToComment(
      @PathVariable String commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.replyToComment(commentId, request))
        .build();
  }

  @GetMapping("/post/{postId}/roots-v2")
  public ApiResponse<Page<CommentResponse>> getRootCommentsV2(
      @PathVariable String postId,
      Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getRootCommentsV2(postId, pageable))
        .build();
  }

  @GetMapping("/{commentId}/replies")
  public ApiResponse<Page<CommentResponse>> getReplies(
      @PathVariable String commentId,
      Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getReplies(commentId, pageable))
        .build();
  }

  @PutMapping("/{commentId}")
  public ApiResponse<CommentResponse> updateComment(
      @PathVariable String commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.updateComment(commentId, request))
        .build();
  }

  // ===== SOFT DELETE =====
  @DeleteMapping("/{commentId}/soft-delete")
  public ApiResponse<CommentResponse> softDeleteComment(
      @PathVariable String commentId) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.softDeleteComment(commentId))
        .build();
  }

  // ===== MY COMMENTS =====
  @GetMapping("/my-comments")
  public ApiResponse<Page<CommentResponse>> getMyComments(Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getMyComments(pageable))
        .build();
  }
}
