package com.graduation.project.library.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.repository.SubjectRepository;
import com.graduation.project.library.dto.SubjectRequest;
import com.graduation.project.library.dto.SubjectResponse;
import com.graduation.project.library.mapper.SubjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubjectService {

  private final SubjectRepository subjectRepository;
  private final SubjectMapper subjectMapper;

  public SubjectResponse createSubject(SubjectRequest request) {
    Subject subject = subjectMapper.toSubject(request);
    Subject saved = subjectRepository.save(subject);
    return subjectMapper.toSubjectResponse(saved);
  }

  public SubjectResponse getSubject(UUID id) {
    Subject subject =
        subjectRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
    return subjectMapper.toSubjectResponse(subject);
  }

  public List<SubjectResponse> getAllSubjects() {
    return subjectRepository.findAll().stream().map(subjectMapper::toSubjectResponse).toList();
  }

  public SubjectResponse updateSubject(UUID id, SubjectRequest request) {
    Subject subject =
        subjectRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
    subject.setSubjectName(request.getSubjectName());
    subject.setSubjectCode(request.getSubjectCode());
    subject.setCredit(request.getCredit());
    subject.setDescription(request.getDescription());
    subject.setLastModifiedDate(LocalDateTime.now());
    Subject updated = subjectRepository.save(subject);
    return subjectMapper.toSubjectResponse(updated);
  }

  public void deleteSubject(UUID id) {
    if (!subjectRepository.existsById(id)) {
      throw new AppException(ErrorCode.SUBJECT_NOT_FOUND);
    }
    subjectRepository.deleteById(id);
  }
}
