package com.graduation.project.forum.controller;

import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ReactionDetailResponse;
import com.graduation.project.forum.dto.ReactionRequest;
import com.graduation.project.forum.dto.ReactionSummary;
import com.graduation.project.forum.service.ReactionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;

  @PostMapping
  public ResponseEntity<String> react(@RequestBody ReactionRequest request) {
    reactionService.toggleReaction(request);

    return ResponseEntity.ok("Success");
  }

  @GetMapping("/summary")
  public ResponseEntity<ReactionSummary> getSummary(
      @RequestParam UUID targetId, @RequestParam TargetType targetType) {
    return ResponseEntity.ok(reactionService.getReactionSummary(targetId, targetType));
  }

  @GetMapping
  public ResponseEntity<Page<ReactionDetailResponse>> getList(
      @RequestParam UUID targetId,
      @RequestParam TargetType targetType,
      @RequestParam(required = false) ReactionType type,
      Pageable pageable) {
    return ResponseEntity.ok(reactionService.getReactions(targetId, targetType, type, pageable));
  }
}
