package com.graduation.project.library.service;

import com.graduation.project.library.dto.SubjectResponse;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.repository.SubjectRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonSubjectService {

  public final SubjectRepository subjectRepository;

  public List<SubjectResponse> searchSubjectByFacultyIdAndSemesterId(
      UUID facultyId, UUID semesterId) {
    List<Subject> subjects =
        subjectRepository.findSubjectByFacultyIdAndSemesterId(facultyId, semesterId);
    return subjects.stream().map(Subject::toSubjectResponse).toList();
  }
}
