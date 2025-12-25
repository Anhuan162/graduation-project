package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.CommentRequest;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/post/{postId}")
  public ApiResponse<CommentResponse> createRootComment(
      @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String postId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.ok(commentService.createRootComment(postId, request));
  }

  @PostMapping("/reply/{commentId}")
  public ApiResponse<CommentResponse> replyToComment(
      @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.replyToComment(commentId, request))
        .build();
  }

  @GetMapping("/post/{postId}/roots-v2")
  public ApiResponse<Page<CommentResponse>> getRootCommentsV2(
      @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String postId,
      Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getRootCommentsV2(postId, pageable))
        .build();
  }

  @GetMapping("/{commentId}/replies")
  public ApiResponse<Page<CommentResponse>> getReplies(
      @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String commentId,
      Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getReplies(commentId, pageable))
        .build();
  }

  @PutMapping("/{commentId}")
  public ApiResponse<CommentResponse> updateComment(
      @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.updateComment(commentId, request))
        .build();
  }

  // ===== SOFT DELETE =====
  @DeleteMapping("/{commentId}/soft-delete")
  public ApiResponse<CommentResponse> softDeleteComment(
      @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String commentId) {
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
