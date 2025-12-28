package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.dto.DetailPostResponse;
import com.graduation.project.forum.dto.PostRequest;
import com.graduation.project.forum.dto.PostResponse;
import com.graduation.project.forum.dto.SearchPostRequest;
import com.graduation.project.forum.dto.UpdatePostStatusRequest;
import com.graduation.project.forum.service.PostService;

import jakarta.validation.Valid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @PostMapping("/topic/{topicId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<PostResponse> createPost(
      @PathVariable UUID topicId, @Valid @RequestBody PostRequest request) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.createPost(topicId, request))
        .build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{postId}/status")
  public ApiResponse<PostResponse> updateStatus(
      @PathVariable UUID postId,
      @Valid @RequestBody UpdatePostStatusRequest request) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.updateStatus(postId, request.getStatus()))
        .build();
  }

  @GetMapping("/{postId}")
  public ApiResponse<PostResponse> getOne(@PathVariable UUID postId) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.getOne(postId))
        .build();
  }

  @PutMapping("/{postId}")
  public ApiResponse<PostResponse> update(
      @PathVariable UUID postId, @Valid @RequestBody PostRequest request) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.update(postId, request))
        .build();
  }

  @DeleteMapping("/{postId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<String> delete(@PathVariable UUID postId) {
    postService.delete(postId);
    return ApiResponse.ok("Deleted successfully");
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{postId}/archive")
  public ApiResponse<PostResponse> softDelete(@PathVariable UUID postId) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.softDelete(postId))
        .build();
  }

  @GetMapping
  public ApiResponse<Page<PostResponse>> searchPosts(
      @ModelAttribute SearchPostRequest request, Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.searchPosts(request, pageable))
        .build();
  }

  @GetMapping("/topic/{topicId}")
  public ApiResponse<Page<DetailPostResponse>> getApprovedPostsByTopic(
      @PathVariable UUID topicId, Pageable pageable) {
    return ApiResponse.<Page<DetailPostResponse>>builder()
        .result(postService.getApprovedPostsByTopic(topicId, pageable))
        .build();
  }

  @GetMapping("/topic/{topicId}/search")
  public ApiResponse<Page<PostResponse>> searchPostsByTopic(
      @PathVariable UUID topicId,
      @RequestParam(required = false) PostStatus postStatus,
      Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.searchPostsByTopic(topicId, postStatus, pageable))
        .build();
  }

  @GetMapping("/user/{userId}")
  public ApiResponse<Page<PostResponse>> getPostsByUserId(
      @PathVariable UUID userId, Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.getPostsByUserId(userId, pageable))
        .build();
  }

  @GetMapping("/my-posts")
  public ApiResponse<Page<PostResponse>> getMyPosts(Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.getMyPosts(pageable))
        .build();
  }
}
