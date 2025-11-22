package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.entity.TopicMember;
import com.graduation.project.common.entity.TopicRole;
import com.graduation.project.user.service.TopicMemberService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topic-members")
@RequiredArgsConstructor
public class TopicMemberController {

  private final TopicMemberService topicMemberService;

  @GetMapping("/topic/{topicId}")
  public ResponseEntity<List<TopicMember>> getMembers(@PathVariable UUID topicId) {
    return ResponseEntity.ok(topicMemberService.getMembers(topicId));
  }

  @PostMapping("/join/{topicId}")
  public ResponseEntity<TopicMember> join(@PathVariable UUID topicId) {
    return ResponseEntity.ok(topicMemberService.joinTopic(topicId));
  }

  @PostMapping("/approve/{memberId}")
  public ResponseEntity<TopicMember> approve(@PathVariable UUID memberId) {
    return ResponseEntity.ok(topicMemberService.approveJoin(memberId));
  }

  @DeleteMapping("/{topicId}/kick/{userId}")
  public ResponseEntity<Void> kick(@PathVariable UUID topicId, @PathVariable UUID userId) {
    topicMemberService.kickMember(topicId, userId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/unapproved-member")
  public ApiResponse<Page<TopicMember>> getUnapprovedMembers(Pageable pageable) {
    return ApiResponse.<Page<TopicMember>>builder()
        .result(topicMemberService.findUnapprovedMember(pageable))
        .build();
  }

  @GetMapping("/approved-member")
  public ApiResponse<Page<TopicMember>> getApprovedMembers(Pageable pageable) {
    return ApiResponse.<Page<TopicMember>>builder()
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
