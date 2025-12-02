package com.graduation.project.forum.controller;

import com.graduation.project.forum.dto.ReportRequest;
import com.graduation.project.forum.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void createReport(@Valid @RequestBody ReportRequest request) {
    reportService.createReport(request);
  }
}
