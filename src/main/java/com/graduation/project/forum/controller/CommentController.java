package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.CommentRequest;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.dto.CommentWithReplyCountResponse;
import com.graduation.project.forum.dto.SearchCommentRequest;
import com.graduation.project.forum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
      @PathVariable String postId, @RequestParam Pageable pageable) {
    return ApiResponse.<Page<CommentWithReplyCountResponse>>builder()
        .result(commentService.getRootComments(postId, pageable))
        .build();
  }

  @GetMapping("/{commentId}/replies")
  public ApiResponse<Page<CommentResponse>> getReplies(
      @PathVariable String commentId, @RequestParam Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.getReplies(commentId, pageable))
        .build();
  }

  @PutMapping("/{commentId}")
  public CommentResponse updateComment(
      @PathVariable String commentId, @RequestBody CommentRequest request) {
    return commentService.updateComment(commentId, request);
  }

  @DeleteMapping("/soft-delete/{commentId}")
  public ApiResponse<String> softDeleteComment(@PathVariable String commentId) {
    commentService.softDeleteComment(commentId);
    return ApiResponse.<String>builder().result("Deleted comment").build();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<CommentResponse>> searchComments(
      @ModelAttribute SearchCommentRequest request, Pageable pageable) {
    return ApiResponse.<Page<CommentResponse>>builder()
        .result(commentService.searchComments(request, pageable))
        .build();
  }
}
