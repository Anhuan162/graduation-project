package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.dto.PostRequest;
import com.graduation.project.forum.dto.PostResponse;
import com.graduation.project.forum.dto.SearchPostRequest;
import com.graduation.project.forum.service.PostService;
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
  public ApiResponse<PostResponse> createPost(
      @PathVariable UUID topicId, @RequestBody PostRequest request) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.createPost(topicId, request))
        .build();
  }

  @GetMapping("/{postId}")
  public ApiResponse<PostResponse> getOne(@PathVariable UUID postId) {
    return ApiResponse.<PostResponse>builder().result(postService.getOne(postId)).build();
  }

  @PutMapping("/{postId}")
  public ApiResponse<PostResponse> update(
      @PathVariable UUID postId, @RequestBody PostRequest request) {
    return ApiResponse.<PostResponse>builder().result(postService.update(postId, request)).build();
  }

  @DeleteMapping("/{postId}")
  public ApiResponse<String> delete(@PathVariable String postId) {
    postService.delete(postId);
    return ApiResponse.<String>builder().result("Deleted successfully").build();
  }

  @DeleteMapping("soft-delete/{postId}")
  public ApiResponse<PostResponse> softDelete(@PathVariable String postId) {

    return ApiResponse.<PostResponse>builder().result(postService.softDelete(postId)).build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ApiResponse<Page<PostResponse>> searchPosts(
      @ModelAttribute SearchPostRequest request, Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.searchPosts(request, pageable))
        .build();
  }

  @GetMapping("/topic/{topicId}")
  public ApiResponse<Page<PostResponse>> getApprovedPostsByTopic(
      @PathVariable UUID topicId, Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.getApprovedPostsByTopic(topicId, pageable))
        .build();
  }

  @PostMapping("/upgrade-post/{postId}")
  public ApiResponse<PostResponse> upgradePostStatus(
      @PathVariable UUID postId, @RequestParam PostStatus postStatus) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.upgradePostStatus(postId, postStatus))
        .build();
  }

  @GetMapping("/topic/{topicId}/search")
  public ApiResponse<Page<PostResponse>> searchPostsByTopic(
      @PathVariable UUID topicId, @RequestParam PostStatus postStatus, Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.searchPostsByTopic(topicId, postStatus, pageable))
        .build();
  }
}
