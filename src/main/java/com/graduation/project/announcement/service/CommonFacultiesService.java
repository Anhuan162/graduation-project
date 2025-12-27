package com.graduation.project.announcement.service;

import com.graduation.project.announcement.dto.FacultyResponse;
import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
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
        return faculties.stream().map(Faculty::toFacultyResponse).toList();
    }
}
