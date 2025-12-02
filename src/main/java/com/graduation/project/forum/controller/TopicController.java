package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.TopicRequest;
import com.graduation.project.forum.dto.TopicResponse;
import com.graduation.project.forum.service.TopicService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
  public ApiResponse<TopicResponse> getOneTopic(@PathVariable UUID topicId) {
    return ApiResponse.<TopicResponse>builder().result(topicService.getOneTopic(topicId)).build();
  }

  @PutMapping("/{topicId}")
  public ApiResponse<TopicResponse> update(
      @PathVariable UUID topicId, @RequestBody TopicRequest request) {
    return ApiResponse.<TopicResponse>builder()
        .result(topicService.update(topicId, request))
        .build();
  }

  @DeleteMapping("/{categoryId}")
  public ApiResponse<String> delete(@PathVariable UUID categoryId) {
    topicService.delete(categoryId);
    return ApiResponse.<String>builder().result("Deleted successfully").build();
  }

  @GetMapping
  public ApiResponse<List<TopicResponse>> getAll() {
    return ApiResponse.<List<TopicResponse>>builder().result(topicService.getAll()).build();
  }

  @GetMapping("/category/{categoryId}")
  public ApiResponse<List<TopicResponse>> getByCategory(@PathVariable UUID categoryId) {
    return ApiResponse.<List<TopicResponse>>builder()
        .result(topicService.getByCategory(categoryId))
        .build();
  }
}
