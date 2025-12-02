package com.graduation.project.library.service;

import com.graduation.project.library.dto.SemesterResponse;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonSemesterService {


    private final SemesterRepository semesterRepository;
    public List<SemesterResponse> getAllSemesters() {
        List<Semester> semesters = semesterRepository.findAll();
        return semesters.stream().map(Semester::toResponse).collect(Collectors.toList());
    }
}
