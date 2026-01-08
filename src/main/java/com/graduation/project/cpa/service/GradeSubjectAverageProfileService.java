package com.graduation.project.cpa.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.cpa.constant.Grade;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import com.graduation.project.cpa.repository.GradeSubjectAverageProfileRepository;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.repository.SemesterRepository;
import com.graduation.project.library.entity.SubjectReference;
import com.graduation.project.library.repository.SubjectReferenceRepository;
import com.graduation.project.library.repository.SubjectRepository;
import com.graduation.project.cpa.dto.GradeSubjectAverageProfileRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
@Log4j2
public class GradeSubjectAverageProfileService {
        private final GradeSubjectAverageProfileRepository gradeSubjectAverageProfileRepository;
        private final SemesterRepository semesterRepository;
        private final FacultyRepository facultyRepository;
        private final SubjectReferenceRepository subjectReferenceRepository;
        private final SubjectRepository subjectRepository;

        public List<GradeSubjectAverageProfile> addGradeSubjectAverageProfileList(
                        int currentSemesterId, String facultyCode, String cohortCode, GpaProfile gpaProfile) {
                Faculty faculty = facultyRepository
                                .findByFacultyCode(facultyCode)
                                .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

                Semester semester = semesterRepository
                                .findById(currentSemesterId)
                                .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

                List<SubjectReference> subjectReferences = subjectReferenceRepository
                                .findAllBySemesterAndFacultyAndCohortCode(
                                                semester, faculty, CohortCode.valueOf(cohortCode));

                List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
                subjectReferences.forEach(
                                subjectReference -> {
                                        GradeSubjectAverageProfile gradeSubjectAverageProfile = new GradeSubjectAverageProfile();
                                        gradeSubjectAverageProfile.setSubjectReference(subjectReference);
                                        gradeSubjectAverageProfile.setGpaProfile(gpaProfile);
                                        gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
                                });

                return gradeSubjectAverageProfiles;
        }

        @Transactional
        public GradeSubjectAverageProfile updateGradeAverageScoreProfile(
                        GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest,
                        GpaProfile gpaProfile) {
                GradeSubjectAverageProfile gradeSubjectAverageProfile;

                if (gradeSubjectAverageProfileRequest.getId() == null) {
                        // ====== CASE: ADD NEW SUBJECT ======

                        // 1. Validate Subject Master
                        if (gradeSubjectAverageProfileRequest.getSubjectId() == null) {
                                throw new AppException(ErrorCode.SUBJECT_NOT_FOUND);
                        }
                        com.graduation.project.library.entity.Subject masterSubject = subjectRepository
                                        .findById(UUID.fromString(gradeSubjectAverageProfileRequest.getSubjectId()))
                                        .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

                        // 2. Extract Context (SAFE WAY - FROM ENTITY)
                        com.graduation.project.common.entity.User user = gpaProfile.getCpaProfile().getUser();
                        Semester semester = gpaProfile.getSemester();

                        if (semester == null) {
                                // Fallback: If semester failed to load (should not happen with correct
                                // migration)
                                throw new RuntimeException(
                                                "Data Integrity Error: GpaProfile must have a linked Semester");
                        }

                        // 3. Logic tách mã sinh viên
                        String studentCode = user.getStudentCode();

                        // Validate format MSSV (Cơ bản)
                        if (studentCode == null || studentCode.length() < 8) {
                                throw new AppException(ErrorCode.INVALID_STUDENT_CODE,
                                                "Mã sinh viên không đúng định dạng: " + studentCode);
                        }

                        // Logic cũ (Safe now)
                        String cohortCodeStr = "D" + studentCode.substring(1, 3);
                        String facultyCode = studentCode.substring(5, 7);
                        CohortCode cohortCode;

                        try {
                                cohortCode = CohortCode.valueOf(cohortCodeStr);
                        } catch (IllegalArgumentException e) {
                                throw new AppException(ErrorCode.INVALID_COHORT_CODE,
                                                "Khóa học không hợp lệ: " + cohortCodeStr);
                        }

                        Faculty faculty = facultyRepository.findByFacultyCode(facultyCode)
                                        .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

                        // 4. 🎯 LAZY LOADING REFERENCE (Clean & Robust)
                        SubjectReference subjectRef = subjectReferenceRepository
                                        .findBySubject_IdAndSemester_IdAndFaculty_FacultyCodeAndCohortCode(
                                                        masterSubject.getId(),
                                                        semester.getId(),
                                                        facultyCode,
                                                        cohortCode)
                                        .orElseGet(() -> {
                                                try {
                                                        log.info("Auto-generating SubjectReference: Subj={} Sem={} Cohort={}",
                                                                        masterSubject.getSubjectCode(),
                                                                        semester.getId(), cohortCode);

                                                        SubjectReference newRef = new SubjectReference();
                                                        newRef.setSubject(masterSubject);
                                                        newRef.setSemester(semester);
                                                        newRef.setFaculty(faculty);
                                                        newRef.setCohortCode(cohortCode);

                                                        return subjectReferenceRepository.save(newRef);
                                                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                                                        // Handle Race Condition
                                                        return subjectReferenceRepository
                                                                        .findBySubject_IdAndSemester_IdAndFaculty_FacultyCodeAndCohortCode(
                                                                                        masterSubject.getId(),
                                                                                        semester.getId(), facultyCode,
                                                                                        cohortCode)
                                                                        .orElseThrow(() -> new AppException(
                                                                                        ErrorCode.UNCATEGORIZED_EXCEPTION));
                                                }
                                        });

                        gradeSubjectAverageProfile = new GradeSubjectAverageProfile();
                        gradeSubjectAverageProfile.setSubjectReference(subjectRef);
                        // GpaProfile will be set by caller
                } else {
                        gradeSubjectAverageProfile = gradeSubjectAverageProfileRepository
                                        .findById(UUID.fromString(gradeSubjectAverageProfileRequest.getId()))
                                        .orElseThrow(() -> new AppException(ErrorCode.GPA_PROFILE_NOT_FOUND));
                }

                gradeSubjectAverageProfile.setLetterCurrentScore(
                                gradeSubjectAverageProfileRequest.getLetterCurrentScore());
                gradeSubjectAverageProfile.setLetterImprovementScore(
                                gradeSubjectAverageProfileRequest.getLetterImprovementScore());
                gradeSubjectAverageProfile.setCurrentScore(
                                Grade.toScore(gradeSubjectAverageProfileRequest.getLetterCurrentScore()));
                gradeSubjectAverageProfile.setImprovementScore(
                                Grade.toScore(gradeSubjectAverageProfileRequest.getLetterImprovementScore()));

                return gradeSubjectAverageProfile;
        }
}
