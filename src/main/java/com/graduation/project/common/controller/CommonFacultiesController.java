package com.graduation.project.common.controller;

import com.graduation.project.admin.dto.FacultyResponse;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.service.CommonFacultiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class CommonFacultiesController {

    private final CommonFacultiesService facultiesService;
    @GetMapping("/all")
    public ApiResponse<List<FacultyResponse>> getAllFaculties() {
        List<FacultyResponse> faculties = facultiesService.getAllFaculties();
        return  ApiResponse.<List<FacultyResponse>>builder().result(faculties).build();
    }
}
