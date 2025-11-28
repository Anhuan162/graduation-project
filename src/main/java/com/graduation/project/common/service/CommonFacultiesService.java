package com.graduation.project.common.service;

import com.graduation.project.admin.dto.FacultyResponse;
import com.graduation.project.common.entity.Faculty;
import com.graduation.project.common.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonFacultiesService {
    private final FacultyRepository facultyRepository;

    public List<FacultyResponse> getAllFaculties() {
        List<Faculty> faculties = facultyRepository.findAll();
        return faculties.stream().map(Faculty::toFacultyResponse).collect(Collectors.toList());
    }
}
