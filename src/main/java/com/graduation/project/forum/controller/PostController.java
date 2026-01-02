package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.constant.TimeRange;
import com.graduation.project.forum.dto.DetailPostResponse;
import com.graduation.project.forum.dto.PostRequest;
import com.graduation.project.forum.dto.PostResponse;
import com.graduation.project.forum.dto.SearchPostRequest;
import com.graduation.project.forum.dto.TrackViewResponse;
import com.graduation.project.forum.dto.UpdatePostStatusRequest;
import com.graduation.project.forum.service.PostService;
import com.graduation.project.forum.service.PostViewService;

import jakarta.validation.Valid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private static final Logger logger = LoggerFactory.getLogger(PostController.class);

  private final PostService postService;

  private final PostViewService postViewService;

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

  @PostMapping("/{postId}/view")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<TrackViewResponse> trackView(@PathVariable UUID postId) {
    return ApiResponse.<TrackViewResponse>builder()
        .result(postViewService.trackView(postId))
        .build();
  }

  @PutMapping("/{postId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<PostResponse> update(
      @PathVariable UUID postId, @Valid @RequestBody PostRequest request) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.update(postId, request))
        .build();
  }

  @DeleteMapping("/{postId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<String> delete(@PathVariable UUID postId) {
    postService.delete(postId);
    return ApiResponse.ok("Deleted permanently");
  }

  @PatchMapping("/{postId}/archive")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<PostResponse> softDelete(@PathVariable UUID postId) {
    return ApiResponse.<PostResponse>builder()
        .result(postService.softDelete(postId))
        .build();
  }

  /**
   * Searches for posts based on various criteria such as keywords, categories,
   * and filters.
   * Supports pagination for efficient data retrieval.
   *
   * @param request  the search criteria including keywords, category, etc.
   * @param pageable pagination information (page, size, sort)
   * @return ApiResponse containing a paginated list of post responses
   */
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

  @GetMapping("/featured")
  public ApiResponse<Page<PostResponse>> getFeaturedPosts(
      @RequestParam(defaultValue = "WEEK") TimeRange range,
      @RequestParam(required = false) UUID topicId,
      Pageable pageable) {
    return ApiResponse.<Page<PostResponse>>builder()
        .result(postService.getFeaturedPosts(range, topicId, pageable))
        .build();
  }

  /**
   * Fixes attachment URLs for a batch of posts to prevent timeouts and memory
   * issues.
   * Processes posts in pages to handle large datasets efficiently.
   *
   * @param page the page number to process (0-based)
   * @param size the number of posts to process per batch
   * @return ApiResponse with the number of records processed
   */
  @PostMapping("/admin/fix-attachment-urls")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<String> fixAttachmentUrls(@RequestParam int page, @RequestParam int size) {
    int processedCount = postService.fixExistingAttachmentUrls(page, size);
    logger.info("Processed {} records for attachment URL fixes (page: {}, size: {})", processedCount, page, size);
    return ApiResponse.ok("Processed " + processedCount + " records for attachment URL fixes");
  }

}
