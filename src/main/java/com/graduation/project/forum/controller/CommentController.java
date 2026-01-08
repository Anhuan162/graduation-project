package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.service.CommentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/post/{postId}")
  public ApiResponse<CommentResponse> createRootComment(
      @PathVariable String postId, @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.createRootComment(postId, request))
        .build();
  }

  @PostMapping("/{parentId}/replies")
  public ApiResponse<CommentResponse> replyToComment(
      @PathVariable String parentId, @RequestBody CommentRequest request) {
    return ApiResponse.<CommentResponse>builder()
        .result(commentService.replyToComment(parentId, request))
        .build();
  }

  @GetMapping("/post/{postId}")
  public ApiResponse<Page<CommentWithReplyCountResponse>> getRootComments(
      @PathVariable String postId, @PageableDefault(page = 0, size = 10) Pageable pageable) {
    return ApiResponse.<Page<CommentWithReplyCountResponse>>builder()
        .result(commentService.getRootComments(postId, pageable))
        .build();
  }

  @GetMapping("/{commentId}/replies")
  public ApiResponse<Page<DetailCommentResponse>> getReplies(
      @PathVariable String commentId, Pageable pageable) {
    return ApiResponse.<Page<DetailCommentResponse>>builder()
        .result(commentService.getReplies(commentId, pageable))
        .build();
  }

  @PutMapping("/{commentId}")
  public CommentResponse updateComment(
      @PathVariable String commentId, @RequestBody CommentRequest request) {
    return commentService.updateComment(commentId, request);
  }

  @DeleteMapping("/soft-delete/{commentId}")
  public ApiResponse<CommentResponse> softDeleteComment(@PathVariable String commentId) {

    return ApiResponse.<CommentResponse>builder()
        .result(commentService.softDeleteComment(commentId))
        .build();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<CommentResponse>> searchComments(
      @ModelAttribute SearchCommentRequest request, Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.searchComments(request, pageable))
        .build();
  }

  @GetMapping("/{commentId}")
  public ApiResponse<DetailCommentResponse> getComment(@PathVariable UUID commentId) {
    return ApiResponse.<DetailCommentResponse>builder()
        .result(commentService.getComment(commentId))
        .build();
  }

  @GetMapping("/my-comments")
  public ApiResponse<Page<CommentResponse>> getMyComments(
      @PageableDefault(page = 0, size = 10) Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getMyComments(pageable))
        .build();
  }

  @GetMapping("/user/{userId}")
  public ApiResponse<Page<CommentResponse>> getUserComments(
      @PathVariable UUID userId, @PageableDefault(page = 0, size = 10) Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getUserComments(userId, pageable))
        .build();
  }

  @PutMapping("/{postId}/comment/{commentId}/toggle-useful")
  public ApiResponse<Void> toggleUseful(@PathVariable UUID postId, @PathVariable UUID commentId) {
    commentService.toggleCommentUseful(postId, commentId);
    return ApiResponse.<Void>builder().build();
  }
}
