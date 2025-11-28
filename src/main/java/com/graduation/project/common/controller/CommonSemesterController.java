package com.graduation.project.common.controller;

import com.graduation.project.admin.dto.SemesterResponse;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.service.CommonSemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/semester")
@RequiredArgsConstructor
public class CommonSemesterController {

    private final CommonSemesterService semesterService;

    @GetMapping("/all")
    public ApiResponse<List<SemesterResponse>> getAllSemesters() {
        List<SemesterResponse> semesters = semesterService.getAllSemesters();
        return ApiResponse.<List<SemesterResponse>>builder().result(semesters).build();
    }
}
