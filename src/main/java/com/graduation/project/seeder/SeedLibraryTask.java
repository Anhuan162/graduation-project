package com.graduation.project.seeder;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.User;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.library.constant.*;
import com.graduation.project.library.entity.*;
import com.graduation.project.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class SeedLibraryTask {

    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectReferenceRepository subjectReferenceRepository;
    private final DocumentRepository documentRepository;

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;

    private final Faker faker = new Faker();

    @Transactional
    public void run(SeedProperties props) {
        log.info("[LibrarySeed] start");

        User uploader = pickAnyUserOrNull();
        if (uploader == null) {
            log.warn("No user found in DB -> Document seed will be skipped (likely NOT NULL constraint).");
        } else {
            log.info("Found uploader user: {}", uploader.getEmail());
        }

        List<Semester> semesters = seedSemesters();
        List<Subject> subjects = seedSubjects();
        List<Faculty> faculties = seedFaculties();
        seedSubjectReferences(subjects, faculties, semesters);
        seedDocuments(subjects, uploader);

        log.info("[LibrarySeed] done. semesters={}, subjects={}, faculties={}, refs={}, docs={}",
                semesters.size(), subjects.size(), faculties.size(), subjectReferenceRepository.count(),
                documentRepository.count());
    }

    private User pickAnyUserOrNull() {
        return userRepository.findAll(PageRequest.of(0, 1))
                .stream().findFirst().orElse(null);
    }

    private List<Semester> seedSemesters() {
        if (semesterRepository.count() > 0)
            return semesterRepository.findAll();

        int baseYear = 2023;

        List<Semester> list = List.of(
                Semester.builder().semesterType(SemesterType.FIRST).schoolYear(baseYear).build(),
                Semester.builder().semesterType(SemesterType.SECOND).schoolYear(baseYear).build(),
                Semester.builder().semesterType(SemesterType.SUMMER).schoolYear(baseYear).build(),
                Semester.builder().semesterType(SemesterType.FIRST).schoolYear(baseYear + 1).build(),
                Semester.builder().semesterType(SemesterType.SECOND).schoolYear(baseYear + 1).build());

        semesterRepository.saveAll(list);
        log.info(" - seeded semesters: {}", list.size());
        return list;
    }

    private List<Subject> seedSubjects() {
        if (subjectRepository.count() > 0)
            return subjectRepository.findAll();

        String[][] seeds = {
                { "Data Structures", "CS201", "3" },
                { "Operating Systems", "CS301", "3" },
                { "Database Systems", "CS305", "3" },
                { "Computer Networks", "CS315", "3" },
                { "Software Engineering", "CS401", "3" },
                { "Web Development", "CS220", "3" },
                { "AI Basics", "CS420", "3" },
        };

        List<Subject> list = new ArrayList<>();
        for (String[] s : seeds) {
            Subject subject = new Subject();
            subject.setSubjectName(s[0]);
            subject.setSubjectCode(s[1]);
            subject.setCredit(Integer.parseInt(s[2]));
            subject.setDescription("Seeded subject: " + s[0]);
            list.add(subject);
        }

        subjectRepository.saveAll(list);
        log.info(" - seeded subjects: {}", list.size());
        return list;
    }

    private List<Faculty> seedFaculties() {
        if (facultyRepository.count() > 0)
            return facultyRepository.findAll();

        String[] names = { "Information Technology", "Business", "Design", "Marketing" };
        List<Faculty> list = new ArrayList<>();
        for (String name : names) {
            Faculty f = new Faculty();
            f.setFacultyName(name);
            list.add(f);
        }

        facultyRepository.saveAll(list);
        log.info(" - seeded faculties: {}", list.size());
        return list;
    }

    private void seedSubjectReferences(List<Subject> subjects, List<Faculty> faculties, List<Semester> semesters) {
        if (subjectReferenceRepository.count() > 0)
            return;

        CohortCode[] cohorts = CohortCode.values();
        List<SubjectReference> list = new ArrayList<>();
        Random rnd = new Random();

        for (Subject subject : subjects) {
            int facultyCount = Math.min(2, faculties.size());
            Collections.shuffle(faculties, rnd);

            for (int i = 0; i < facultyCount; i++) {
                Faculty faculty = faculties.get(i);
                Semester semester = semesters.get(rnd.nextInt(semesters.size()));
                CohortCode cohort = cohorts[rnd.nextInt(cohorts.length)];

                boolean exists = subjectReferenceRepository
                        .existsBySubjectIdAndFacultyIdAndSemesterIdAndCohortCode(
                                subject.getId(),
                                faculty.getId(),
                                semester.getId(),
                                cohort);

                if (exists)
                    continue;

                SubjectReference ref = new SubjectReference();
                ref.setSubject(subject);
                ref.setFaculty(faculty);
                ref.setSemester(semester);
                ref.setCohortCode(cohort);
                list.add(ref);
            }
        }

        subjectReferenceRepository.saveAll(list);
        log.info(" - seeded subject references: {}", list.size());
    }

    private void seedDocuments(List<Subject> subjects, User uploader) {
        if (documentRepository.count() > 0)
            return;
        if (uploader == null) {
            log.warn(" - skipped documents seeding because uploader is null");
            return;
        }

        int perSubject = 6;
        List<Document> docs = new ArrayList<>();
        Random rnd = new Random();

        // Valid statuses for seeding (excluding PROCESSING and FAILED as they are
        // transient states)
        DocumentStatus[] validStatuses = {
                DocumentStatus.PUBLISHED, // 60% of documents
                DocumentStatus.PUBLISHED,
                DocumentStatus.PUBLISHED,
                DocumentStatus.PENDING, // 30% of documents
                DocumentStatus.PENDING,
                DocumentStatus.REJECTED // 10% of documents
        };

        for (Subject subject : subjects) {
            for (int i = 0; i < perSubject; i++) {
                DocumentType type = DocumentType.values()[rnd.nextInt(DocumentType.values().length)];
                String title = type.name() + " - " + subject.getSubjectCode() + " - " + faker.book().title();

                String filePath = "/uploads/docs/" + subject.getSubjectCode().toLowerCase()
                        + "/" + UUID.randomUUID() + ".pdf";

                String checksum = sha256(title + "|" + filePath);

                // Select a valid status for this document
                DocumentStatus status = validStatuses[i % validStatuses.length];

                Document doc = Document.builder()
                        .title(title)
                        .description("Seeded document for " + subject.getSubjectName())
                        .filePath(filePath)
                        .subject(subject)
                        .uploadedBy(uploader)
                        .approvedBy(status == DocumentStatus.PUBLISHED ? uploader : null)
                        .documentStatus(status) // Always use a valid enum value
                        .documentType(type)
                        .size(200_000 + rnd.nextInt(2_000_000))
                        .originalFilename(subject.getSubjectCode() + "-" + type.name().toLowerCase() + ".pdf")
                        .storageProvider("local")
                        .mimeType("application/pdf")
                        .checksum(checksum)
                        .visibility(VisibilityStatus.PUBLIC)
                        .downloadCount(rnd.nextInt(300))
                        .createdAt(LocalDateTime.now().minusDays(rnd.nextInt(60)))
                        .updatedAt(LocalDateTime.now())
                        .approvedAt(status == DocumentStatus.PUBLISHED ? LocalDateTime.now().minusDays(rnd.nextInt(30))
                                : null)
                        .rejectionReason(status == DocumentStatus.REJECTED ? "Seeded rejection: Quality issue" : null)
                        .imageUrl("https://picsum.photos/seed/" + checksum.substring(0, 8) + "/640/360")
                        .build();

                docs.add(doc);
            }
        }

        documentRepository.saveAll(docs);
        log.info(" - seeded documents: {} (PUBLISHED: ~60%, PENDING: ~30%, REJECTED: ~10%)", docs.size());
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}
