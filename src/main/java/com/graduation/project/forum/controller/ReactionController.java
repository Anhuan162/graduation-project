package com.graduation.project.forum.controller;

import com.graduation.project.forum.dto.ReactionRequest;
import com.graduation.project.forum.service.ReactionService;
import lombok.RequiredArgsConstructor;
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
}
