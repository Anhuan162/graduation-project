package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.PostRequest;
import com.graduation.project.user.dto.PostResponse;
import com.graduation.project.user.service.PostService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

  @GetMapping
  public ApiResponse<List<PostResponse>> getAll() {
    return ApiResponse.<List<PostResponse>>builder().result(postService.getAll()).build();
  }

  @GetMapping("/topic/{topicId}")
  public ApiResponse<List<PostResponse>> getByTopic(@PathVariable UUID topicId) {
    return ApiResponse.<List<PostResponse>>builder()
        .result(postService.getByTopic(topicId))
        .build();
  }

  @PostMapping("/{postId}/approve")
  public ApiResponse<PostResponse> approve(@PathVariable UUID postId) {
    return ApiResponse.<PostResponse>builder().result(postService.approvePost(postId)).build();
  }

  @PostMapping("/{postId}/reject")
  public ApiResponse<PostResponse> reject(@PathVariable UUID postId) {
    return ApiResponse.<PostResponse>builder().result(postService.rejectPost(postId)).build();
  }

  @GetMapping("/topic/{topicId}/pending")
  public ApiResponse<List<PostResponse>> getPending(@PathVariable UUID topicId) {
    return ApiResponse.<List<PostResponse>>builder()
        .result(postService.getPendingByTopic(topicId))
        .build();
  }

  @GetMapping("/topic/{topicId}/approved")
  public ApiResponse<List<PostResponse>> getApproved(@PathVariable UUID topicId) {
    return ApiResponse.<List<PostResponse>>builder()
        .result(postService.getApprovedByTopic(topicId))
        .build();
  }

  @GetMapping("/topic/{topicId}/rejected")
  public ApiResponse<List<PostResponse>> getRejected(@PathVariable UUID topicId) {
    return ApiResponse.<List<PostResponse>>builder()
        .result(postService.getRejectedByTopic(topicId))
        .build();
  }
}
