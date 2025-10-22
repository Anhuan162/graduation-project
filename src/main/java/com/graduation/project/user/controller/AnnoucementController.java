package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.AnnoucementResponse;
import com.graduation.project.user.service.AnnoucementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AnnoucementController {

  private final AnnoucementService annoucementService;

  @GetMapping
  public ApiResponse<List<AnnoucementResponse>> getAnnoucements() {
    return ApiResponse.<List<AnnoucementResponse>>builder()
        .result(annoucementService.getAllAnnoucements())
        .build();
  }
}
