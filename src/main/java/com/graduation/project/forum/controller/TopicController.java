package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.DetailTopicResponse;
import com.graduation.project.forum.dto.SearchTopicRequest;
import com.graduation.project.forum.dto.TopicRequest;
import com.graduation.project.forum.dto.TopicResponse;
import com.graduation.project.forum.service.TopicService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

  private final TopicService topicService;

  @PostMapping("/category/{categoryId}")
  public ApiResponse<TopicResponse> create(
      @PathVariable UUID categoryId, @RequestBody TopicRequest request) {
    return ApiResponse.<TopicResponse>builder()
        .result(topicService.create(categoryId, request))
        .build();
  }

  @GetMapping("/{topicId}")
  public ApiResponse<DetailTopicResponse> getOneTopic(@PathVariable UUID topicId) {
    return ApiResponse.<DetailTopicResponse>builder()
        .result(topicService.getOneTopic(topicId))
        .build();
  }

  @PutMapping("/{topicId}")
  public ApiResponse<TopicResponse> update(
      @PathVariable UUID topicId, @RequestBody TopicRequest request) {
    return ApiResponse.<TopicResponse>builder()
        .result(topicService.update(topicId, request))
        .build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{topicId}")
  public ApiResponse<String> delete(@PathVariable UUID topicId) {
    topicService.delete(topicId);
    return ApiResponse.<String>builder().result("Deleted successfully").build();
  }

  @DeleteMapping("soft-delete/{topicId}")
  public ApiResponse<TopicResponse> softDelete(@PathVariable UUID topicId) {
    return ApiResponse.<TopicResponse>builder().result(topicService.softDelete(topicId)).build();
  }

  @GetMapping
  public ApiResponse<Page<TopicResponse>> searchTopics(
      @ModelAttribute SearchTopicRequest request, Pageable pageable) {
    return ApiResponse.<Page<TopicResponse>>builder()
        .result(topicService.searchTopics(request, pageable))
        .build();
  }

  @GetMapping("/category/{categoryId}")
  public ApiResponse<Page<TopicResponse>> getByCategory(
      @PathVariable UUID categoryId, Pageable pageable) {
    return ApiResponse.<Page<TopicResponse>>builder()
        .result(topicService.getByCategory(categoryId, pageable))
        .build();
  }
}
