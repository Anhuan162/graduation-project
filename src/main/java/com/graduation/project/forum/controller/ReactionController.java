package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ReactionDetailResponse;
import com.graduation.project.forum.dto.ReactionRequest;
import com.graduation.project.forum.dto.ReactionSummary;
import com.graduation.project.forum.dto.ReactionToggleResponse;
import com.graduation.project.forum.service.ReactionService;

import jakarta.validation.Valid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;

  @PostMapping("/toggle")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<ReactionToggleResponse> toggle(@Valid @RequestBody ReactionRequest req) {
    return ApiResponse.<ReactionToggleResponse>builder()
        .result(reactionService.toggleReaction(req))
        .build();
  }

  @GetMapping("/summary")
  public ApiResponse<ReactionSummary> getSummary(
      @RequestParam UUID targetId, @RequestParam TargetType targetType) {
    return ApiResponse.<ReactionSummary>builder()
        .result(reactionService.getReactionSummary(targetId, targetType))
        .build();
  }

  @GetMapping
  public ApiResponse<Page<ReactionDetailResponse>> getList(
      @RequestParam UUID targetId,
      @RequestParam TargetType targetType,
      @RequestParam(required = false) ReactionType type,
      Pageable pageable) {
    return ApiResponse.<Page<ReactionDetailResponse>>builder()
        .result(reactionService.getReactions(targetId, targetType, type, pageable))
        .build();
  }
}
