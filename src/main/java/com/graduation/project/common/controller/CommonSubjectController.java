package com.graduation.project.common.controller;

import com.graduation.project.admin.dto.SubjectResponse;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.common.service.CommonSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subject")
@RequiredArgsConstructor
public class CommonSubjectController {

    private final CommonSubjectService subjectService;

    @GetMapping("/search")
    public ApiResponse<List<SubjectResponse>> search(
            @RequestParam()String facultyId,
            @RequestParam()String semesterId
            ) {
        UUID UUIDfacultyId = null;
        UUID UUIDsemesterId = null;
        try {
            UUIDsemesterId = UUID.fromString(semesterId);
        } catch (Exception e) {}

        try {
            UUIDfacultyId = UUID.fromString(facultyId);
        } catch (Exception e) {}
        List<SubjectResponse> res =
                subjectService.searchSubjectByFacultyIdAndSemesterId(UUIDfacultyId, UUIDsemesterId);
        return ApiResponse.<List<SubjectResponse>>builder().result(res).build();
    }
}
