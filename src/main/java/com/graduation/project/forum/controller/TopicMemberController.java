package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.dto.TopicMemberResponse;
import com.graduation.project.forum.service.TopicMemberService;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topic-members")
@RequiredArgsConstructor
public class TopicMemberController {

  private final TopicMemberService topicMemberService;

  @GetMapping("/topic/{topicId}")
  public ApiResponse<Page<TopicMemberResponse>> getMembers(
      @PathVariable UUID topicId,
      @RequestParam(required = false) Boolean approved,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ApiResponse.<Page<TopicMemberResponse>>builder()
        .result(topicMemberService.getMembers(topicId, approved, pageable))
        .build();
  }

  @PostMapping("/join/{topicId}")
  public ApiResponse<TopicMemberResponse> join(@PathVariable UUID topicId) {
    return ApiResponse.<TopicMemberResponse>builder()
        .result(topicMemberService.joinTopic(topicId))
        .build();
  }

  @PostMapping("/approve/{memberId}")
  public ApiResponse<TopicMemberResponse> approve(@PathVariable UUID memberId) {
    return ApiResponse.<TopicMemberResponse>builder()
        .result(topicMemberService.approveJoin(memberId))
        .build();
  }

  @DeleteMapping("/{topicId}/kick/{userId}")
  public ApiResponse<String> kick(@PathVariable UUID topicId, @PathVariable UUID userId) {
    topicMemberService.kickMember(topicId, userId);
    return ApiResponse.<String>builder()
        .result("Member kicked successfully")
        .build();
  }

  @GetMapping("/unapproved-member")
  public ApiResponse<Page<TopicMemberResponse>> getUnapprovedMembers(Pageable pageable) {
    return ApiResponse.<Page<TopicMemberResponse>>builder()
        .result(topicMemberService.findUnapprovedMember(pageable))
        .build();
  }

  @GetMapping("/approved-member")
  public ApiResponse<Page<TopicMemberResponse>> getApprovedMembers(Pageable pageable) {
    return ApiResponse.<Page<TopicMemberResponse>>builder()
        .result(topicMemberService.findApprovedMember(pageable))
        .build();
  }

  @PostMapping("/{topicId}/managers/{userId}")
  public ApiResponse<String> addTopicMember(
      @PathVariable UUID topicId,
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "MEMBER") String topicRole) {
    topicMemberService.addTopicMember(topicId, userId, topicRole);
    return ApiResponse.<String>builder().result("User added as manager successfully").build();
  }

  @PutMapping("/{topicMemberId}")
  public ApiResponse<String> updateTopicMember(
      @PathVariable UUID topicMemberId, @RequestParam TopicRole topicRole) {
    topicMemberService.updateTopicMember(topicMemberId, topicRole);
    return ApiResponse.<String>builder().result("User added as manager successfully").build();
  }

  @DeleteMapping("/remove/{topicMemberId}")
  public ApiResponse<String> removeManager(@PathVariable UUID topicMemberId) {
    topicMemberService.removeTopicMember(topicMemberId);
    return ApiResponse.<String>builder().result("User removed from managers successfully").build();
  }
}
