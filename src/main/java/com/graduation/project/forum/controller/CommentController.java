package com.graduation.project.forum.controller;

import com.graduation.project.forum.dto.CommentRequest;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.dto.CommentWithReplyCountResponse;
import com.graduation.project.forum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/posts/{postId}/comments")
  public CommentResponse createRootComment(
      @PathVariable String postId, @RequestBody CommentRequest request) {
    return commentService.createRootComment(postId, request);
  }

  @PostMapping("/comments/{parentId}/replies")
  public CommentResponse replyToComment(
      @PathVariable String parentId, @RequestBody CommentRequest request) {
    return commentService.replyToComment(parentId, request);
  }

  @GetMapping("/post/{postId}")
  public Page<CommentWithReplyCountResponse> getRootComments(
      @PathVariable String postId, @RequestParam Pageable pageable) {
    return commentService.getRootComments(postId, pageable);
  }

//  @PutMapping("/comments/{commentId}")
//  public CommentResponse updateComment(
//      @PathVariable String commentId, @RequestBody CommentRequest request) {
//    return commentService.updateComment(commentId, request);
//  }

  @DeleteMapping("/comments/{commentId}")
  public void deleteComment(@PathVariable String commentId) {
    commentService.deleteComment(commentId);
  }

  @GetMapping("/{commentId}/replies")
  public Page<CommentResponse> getReplies(
      @PathVariable String commentId, @RequestParam Pageable pageable) {
    return commentService.getReplies(commentId, pageable);
  }
}
