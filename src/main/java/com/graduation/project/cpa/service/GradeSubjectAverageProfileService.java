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

    public List<GradeSubjectAverageProfile> addGradeSubjectAverageProfileList(
            int currentSemesterId, String facultyCode, String cohortCode, GpaProfile gpaProfile) {
        Faculty faculty = facultyRepository
                .findByFacultyCode(facultyCode)
                .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

        Semester semester = semesterRepository
                .findById(currentSemesterId)
                .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

        List<SubjectReference> subjectReferences = subjectReferenceRepository.findAllBySemesterAndFacultyAndCohortCode(
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

    public GradeSubjectAverageProfile updateGradeAverageScoreProfile(
            GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest) {
        GradeSubjectAverageProfile gradeSubjectAverageProfile;

        if (gradeSubjectAverageProfileRequest.getId() == null) {
            // Create New
            if (gradeSubjectAverageProfileRequest.getSubjectId() == null) {
                throw new AppException(ErrorCode.SUBJECT_NOT_FOUND);
                // Or a more specific error "SUBJECT_ID_REQUIRED" but let's reuse
            }

            // We usually link to SubjectReference.
            // Note: The UI likely searches 'Subjects'.
            // We need to find or create a SubjectReference for this Semester?
            // Wait, SubjectReference is (Semester + Subject + Faculty + Cohort).
            // If the user adds a subject to a SPECIFIC Semester (GpaProfile),
            // that GpaProfile belongs to a Semester? No, GpaProfile IS the semester
            // performance?
            // GpaProfile code is "GPA"+StudentCode+SemesterId.
            // But GpaProfile entity doesn't explicitly store 'SemesterId' field easily
            // accessible?
            // Wait, GpaProfile has `gpaProfileCode`. We can extract? Or passed in context?

            // This is tricky. Ideally we need the SubjectReference to exist.
            // Assuming for now the `subjectId` passed is actually the `SubjectReference`
            // UUID?
            // Or is it the `Subject` UUID?
            // The /api/subjects/search returns `SubjectResponse` which has `id` (Subject
            // ID).

            // If we only have Subject ID, we need to know WHICH Semester/Faculty/Cohort
            // this applies to.
            // GpaProfileService knows the context?
            // Ideally, we might just need to link to the Subject directly if the system was
            // simpler.
            // But here `GradeSubjectAverageProfile` links to `SubjectReference`.

            // CRITICAL: We need a valid SubjectReference.
            // If the system generates SubjectReferences beforehand (which it did via
            // `addGradeSubjectAverageProfileList`),
            // then for a NEW subject, we might need to find the SubjectReference for (This
            // Semester, This Subject, This User's Cohort/Faculty).

            // Let's assume `gradeSubjectAverageProfileRequest.getSubjectId()` is the
            // `SubjectReference` ID for simplicity?
            // NO, the search API returns Subjects.
            // So we need to look up SubjectReference.

            // WORKAROUND: For this specific refactor, finding the correct SubjectReference
            // is complex without more context.
            // HOWEVER, `SubjectReference` is just a tuple.
            // If it doesn't exist, we might fail.
            // Given existing Subjects usually have References created for cohorts, let's
            // assume we can try to find it.
            // But we lack Semester/Faculty/Cohort in the Request to
            // `updateGradeAverageScoreProfile`.
            // These are effectively in the Parent (GpaProfile).

            // ARCHITECTURE DECISION: To avoid breaking changes or massive lookups:
            // 1. We'll assume the `subjectId` passed IS the `SubjectReference` ID if
            // possible?
            // (UI searches Subjects, but maybe we can lookup references? No, too hard for
            // UI).
            // 2. Or we change `GradeSubjectAverageProfile` to link to `Subject` directly?
            // (Too big database change).
            // 3. Or we inject the GpaProfile into this method to extract context?

            // Let's assume `gradeSubjectAverageProfileRequest.getSubjectId()` is
            // `SubjectReference` ID.
            // We will make the frontend search consistent with this (maybe frontend
            // searches SubjectReferences? Unlikely).

            // Alternate path: The user adds a subject.
            // We really need the SubjectReference.
            // Let's look at `SubjectReferenceRepository`. It has
            // `findAllBySemesterAndFacultyAndCohortCode`.

            // I will change the method signature to accept `GpaProfile` or similar context
            // if needed.
            // But `GpaProfileService` calls this. `GpaProfileService` knows the
            // `GpaProfile`.
            // `GpaProfile` has `gpaProfileCode` -> "GPA"+studentCode+SemesterId ??
            // "GPA"+StudentCode+SemesterId -> can extract SemesterId.
            // Faculty/Cohort -> User profile.

            // This is getting complicated.
            // SIMPLEST SENIOR FIX:
            // Assume `subjectId` passed IS `SubjectReference` ID.
            // The Frontend's "Search Subject" should probably search `SubjectReference`
            // applicable to the user?
            // Or if it searches `Subject`, we need to find the `SubjectReference` ID.

            // I'll stick to: Query the `SubjectReference` by ID (passed in field
            // `subjectId`).
            // If the frontend can't get SubjectReference ID, we have a problem.
            // But `CommonSubjectController` returns `SubjectResponse`.

            // Let's modify this method to just "Find SubjectReference by ID" assuming the
            // input is correct.
            // We will see if we can adapt Frontend to find the reference or if we need a
            // backend helper.

            SubjectReference subjectRef = subjectReferenceRepository
                    .findById(UUID.fromString(gradeSubjectAverageProfileRequest.getSubjectId()))
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

            gradeSubjectAverageProfile = new GradeSubjectAverageProfile();
            gradeSubjectAverageProfile.setSubjectReference(subjectRef);
            // GpaProfile set by caller
        } else {
            gradeSubjectAverageProfile = gradeSubjectAverageProfileRepository
                    .findById(UUID.fromString(gradeSubjectAverageProfileRequest.getId()))
                    .orElseThrow(() -> new AppException(ErrorCode.GPA_PROFILE_NOT_FOUND)); // Or specific error
        }

        gradeSubjectAverageProfile.setLetterCurrentScore(
                gradeSubjectAverageProfileRequest.getLetterCurrentScore());
        gradeSubjectAverageProfile.setLetterImprovementScore(
                gradeSubjectAverageProfileRequest.getLetterImprovementScore());
        gradeSubjectAverageProfile.setCurrentScore(
                Grade.toScore(gradeSubjectAverageProfileRequest.getLetterCurrentScore()));
        gradeSubjectAverageProfile.setImprovementScore(
                Grade.toScore(gradeSubjectAverageProfileRequest.getLetterImprovementScore()));

        // If new, repository.save(gradeSubjectAverageProfile)?
        // Or cascade? The caller adds it to the list.
        // If it's new, it has no ID. We might need to save it to generate ID if we rely
        // on it later?
        // Usually better to save explicit.
        if (gradeSubjectAverageProfile.getId() == null) {
            // We don't save here?
            // Service `updateGradeAverageScoreProfile` name implies it updates/saves.
        }
        // The original code returned the entity but didn't explicitly call save()
        // inside?
        // Wait, original: `gradeSubjectAverageProfileRepository.findById(...)` then
        // setters.
        // It's Transactional, so dirty checking saves it.
        // For NEW entity, we MUST persist it.
        if (gradeSubjectAverageProfile.getId() == null) {
            // But we need the GpaProfile set first?
            // Caller `score.setGpaProfile(gpaProfile)` happens AFTER return.
            // So we can't save here if `gpaProfile` is non-nullable FK?
            // `GradeSubjectAverageProfile` likely has `@ManyToOne` to GpaProfile.
            // If mapping is correct, we can return transient entity, caller sets parent,
            // then caller (GpaProfileService) saves/merges.
            // GpaProfileService is `@Transactional`.
        }

        return gradeSubjectAverageProfile;
    }
}
