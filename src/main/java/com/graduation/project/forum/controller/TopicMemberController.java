package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.TopicMemberResponse;
import com.graduation.project.forum.dto.UpdateTopicMemberRoleRequest;
import com.graduation.project.forum.service.TopicMemberService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topic-members")
@RequiredArgsConstructor
public class TopicMemberController {

  private final TopicMemberService topicMemberService;

  @GetMapping("/topic/{topicId}")
  public ApiResponse<List<TopicMemberResponse>> getMembers(@PathVariable UUID topicId) {
    return ApiResponse.ok(topicMemberService.getMembers(topicId));
  }

  @PostMapping("/join/{topicId}")
  public ApiResponse<TopicMemberResponse> join(@PathVariable UUID topicId) {
    return ApiResponse.ok(topicMemberService.joinTopic(topicId));
  }

  @PostMapping("/approve/{memberId}")
  public ApiResponse<TopicMemberResponse> approve(@PathVariable UUID memberId) {
    return ApiResponse.ok(topicMemberService.approveJoin(memberId));
  }

  @DeleteMapping("/{topicId}/kick/{userId}")
  public ApiResponse<String> kick(@PathVariable UUID topicId, @PathVariable UUID userId) {
    topicMemberService.kickMember(topicId, userId);
    return ApiResponse.ok("Kicked successfully");
  }

  @GetMapping("/unapproved-member")
  public ApiResponse<Page<TopicMemberResponse>> getUnapprovedMembers(Pageable pageable) {
    return ApiResponse.ok(topicMemberService.findUnapprovedMember(pageable));
  }

  @GetMapping("/approved-member")
  public ApiResponse<Page<TopicMemberResponse>> getApprovedMembers(Pageable pageable) {
    return ApiResponse.ok(topicMemberService.findApprovedMember(pageable));
  }

  @PostMapping("/{topicId}/managers/{userId}")
  public ApiResponse<String> addTopicMember(
      @PathVariable UUID topicId,
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateTopicMemberRoleRequest request) {
    topicMemberService.addTopicMember(topicId, userId, request.getTopicRole());
    return ApiResponse.ok("User added to topic successfully");
  }

  @PutMapping("/{topicMemberId}")
  public ApiResponse<String> updateTopicMember(
      @PathVariable UUID topicMemberId,
      @Valid @RequestBody UpdateTopicMemberRoleRequest request) {
    topicMemberService.updateTopicMember(topicMemberId, request.getTopicRole());
    return ApiResponse.ok("Updated topic member successfully");
  }

  @DeleteMapping("/{topicMemberId}")
  public ApiResponse<String> removeMember(@PathVariable UUID topicMemberId) {
    topicMemberService.removeTopicMember(topicMemberId);
    return ApiResponse.ok("Removed topic member successfully");
  }
}
