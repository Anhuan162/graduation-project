package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.TopicRequest;
import com.graduation.project.user.dto.TopicResponse;
import com.graduation.project.user.service.TopicService;
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
      @PathVariable String categoryId, @RequestBody TopicRequest request) {
    return ApiResponse.<TopicResponse>builder()
        .result(topicService.create(UUID.fromString(categoryId), request))
        .build();
  }

  @GetMapping("/{topicId}")
  public ApiResponse<TopicResponse> getOneTopic(@PathVariable String topicId) {
    return ApiResponse.<TopicResponse>builder().result(topicService.getOneTopic(topicId)).build();
  }

  @PutMapping("/{topicId}")
  public ApiResponse<TopicResponse> update(
      @PathVariable String topicId, @RequestBody TopicRequest request) {
    return ApiResponse.<TopicResponse>builder()
        .result(topicService.update(topicId, request))
        .build();
  }

  @DeleteMapping("/{categoryId}")
  public ApiResponse<String> delete(@PathVariable String categoryId) {
    topicService.delete(categoryId);
    return ApiResponse.<String>builder().result("Deleted successfully").build();
  }

  @GetMapping
  public ApiResponse<List<TopicResponse>> getAll() {
    return ApiResponse.<List<TopicResponse>>builder().result(topicService.getAll()).build();
  }

  @GetMapping("/category/{categoryId}")
  public ApiResponse<List<TopicResponse>> getByCategory(@PathVariable String categoryId) {
    return ApiResponse.<List<TopicResponse>>builder()
        .result(topicService.getByCategory(UUID.fromString(categoryId)))
        .build();
  }
}
