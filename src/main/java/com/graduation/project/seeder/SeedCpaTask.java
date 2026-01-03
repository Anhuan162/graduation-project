package com.graduation.project.seeder;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.User;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.cpa.constant.Grade;
import com.graduation.project.cpa.entity.CpaProfile;
import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import com.graduation.project.cpa.repository.CpaProfileRepository;
import com.graduation.project.cpa.repository.GpaProfileRepository;
import com.graduation.project.cpa.repository.GradeSubjectAverageProfileRepository;
import com.graduation.project.cpa.service.GradeSubjectAverageProfileService;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class SeedCpaTask {

    private final UserRepository userRepository;
    private final CpaProfileRepository cpaProfileRepository;
    private final GpaProfileRepository gpaProfileRepository;
    private final GradeSubjectAverageProfileRepository gradeRepo;

    private final SemesterRepository semesterRepository;
    private final FacultyRepository facultyRepository;

    private final GradeSubjectAverageProfileService gradeSubjectAverageProfileService;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    private static final int TARGET_STUDENT_COUNT = 30;

    private static final String DEFAULT_FACULTY_CODE = "CN";
    private static final String DEFAULT_COHORT_PREFIX = "B20DCCN";

    @Transactional
    public void run(SeedProperties props) {
        log.info(" [CPA] Seeding CPA/GPA started...");

        Faculty faculty = ensureFacultyCN();

        List<Semester> semesters = semesterRepository.findAll();
        if (semesters.isEmpty()) {
            log.warn(" [CPA] Không có Semester trong DB. CPA seed cần Semester trước.");
            log.warn(" Hãy seed module library (Semester/Subject/SubjectReference) trước rồi chạy lại.");
            return;
        }

        List<User> students = pickOrCreateStudents(TARGET_STUDENT_COUNT);

        int created = 0;
        for (User u : students) {
            if (cpaProfileRepository.findAllByUserId(u.getId()).size() > 0)
                continue;

            CpaProfile cpa = new CpaProfile();
            cpa.setCpaProfileName(u.getFullName() != null ? u.getFullName() : "Student " + u.getEmail());
            cpa.setCpaProfileCode("CPA-" + u.getId().toString().substring(0, 8));
            cpa.setCreatedAt(LocalDateTime.now());
            cpa.setUser(u);

            List<GpaProfile> gpas = new ArrayList<>();

            String cohortCode = toCohortCodeFromStudentCode(u.getStudentCode());
            try {
                CohortCode.valueOf(cohortCode);
            } catch (IllegalArgumentException e) {
                log.debug("Skipping CPA/GPA for user {} - Invalid CohortCode: {}", u.getEmail(), cohortCode);
                continue;
            }

            for (Semester s : semesters) {
                GpaProfile gpa = new GpaProfile();
                gpa.setGpaProfileCode("GPA-" + s.getId() + "-" + u.getId().toString().substring(0, 6));
                gpa.setCpaProfile(cpa);

                List<GradeSubjectAverageProfile> grades = gradeSubjectAverageProfileService
                        .addGradeSubjectAverageProfileList(
                                s.getId(), faculty.getFacultyCode(), cohortCode, gpa);

                for (GradeSubjectAverageProfile gr : grades) {
                    String letter = randomLetterGrade();
                    gr.setLetterCurrentScore(letter);
                    gr.setCurrentScore(Grade.toScore(letter));

                    if (random.nextInt(100) < 30) {
                        String improve = randomBetterOrSame(letter);
                        gr.setLetterImprovementScore(improve);
                        gr.setImprovementScore(Grade.toScore(improve));
                    }
                }

                gpa.setGradeSubjectAverageProfiles(new ArrayList<>(grades));
                computeGpa(gpa);
                gpas.add(gpa);
            }

            cpa.setGpaProfiles(gpas);
            computeCpa(cpa);

            cpaProfileRepository.save(cpa);
            for (GpaProfile g : gpas) {
                gpaProfileRepository.save(g);
                gradeRepo.saveAll(g.getGradeSubjectAverageProfiles());
            }

            created++;
        }

        log.info(" [CPA] Seeding CPA/GPA completed. Created {} CPA profiles.", created);
    }

    // ====== giữ nguyên helper methods từ file cũ (ensureFacultyCN,
    // pickOrCreateStudents, computeGpa, computeCpa, ...) ======

    private Faculty ensureFacultyCN() {
        return facultyRepository.findByFacultyCode(DEFAULT_FACULTY_CODE)
                .orElseGet(() -> {
                    Faculty f = new Faculty();
                    f.setFacultyCode(DEFAULT_FACULTY_CODE);
                    f.setFacultyName("Computer Science");
                    f.setDescription("Seeded by SeedCpaTask");
                    Faculty saved = facultyRepository.save(f);
                    log.info(" - [CPA] Seeded Faculty {}", DEFAULT_FACULTY_CODE);
                    return saved;
                });
    }

    private List<User> pickOrCreateStudents(int target) {
        List<User> students = userRepository.findByStudentCodeIsNotNull(PageRequest.of(0, target));
        students = new ArrayList<>(students);

        while (students.size() < target) {
            int idx = students.size() + 1;

            String email = "student" + idx + "@grad.local";
            String studentCode = DEFAULT_COHORT_PREFIX + String.format("%03d", idx);

            User u = new User();
            u.setEmail(email);
            u.setFullName(faker.name().fullName());
            u.setAvatarUrl(faker.avatar().image());
            u.setStudentCode(studentCode);
            u.setClassCode("D20CQCN" + String.format("%02d", (idx % 10) + 1));
            u.setProvider(Provider.LOCAL);

            // tuỳ entity em, đoạn reflection giữ như cũ nếu cần
            // ...
            User saved = userRepository.save(u);
            students.add(saved);
        }
        return students;
    }

    private String toCohortCodeFromStudentCode(String studentCode) {
        if (studentCode == null || studentCode.length() < 3)
            return "D20";
        return "D" + studentCode.substring(1, 3);
    }

    private void computeGpa(GpaProfile gpa) {
        int passedCredit = 0;
        double totalWeighted = 0.0;

        for (GradeSubjectAverageProfile gr : gpa.getGradeSubjectAverageProfiles()) {
            Double avg = gr.getImprovementScore() != null ? gr.getImprovementScore() : gr.getCurrentScore();
            if (avg != null) {
                if (gr.getSubjectReference() == null || gr.getSubjectReference().getSubject() == null) {
                    log.warn("Skipping GradeSubjectAverageProfile {} due to null subject reference or subject",
                            gr.getId());
                    continue;
                }
                int credit = gr.getSubjectReference().getSubject().getCredit();
                passedCredit += credit;
                totalWeighted += credit * avg;
            }
        }

        gpa.setPassedCredits(passedCredit);
        gpa.setTotalWeightedScore(totalWeighted);

        if (passedCredit == 0) {
            gpa.setNumberGpaScore(null);
            gpa.setLetterGpaScore(null);
        } else {
            double number = totalWeighted / passedCredit;
            gpa.setNumberGpaScore(number);
            gpa.setLetterGpaScore(Grade.fromScore(number));
        }
    }

    private void computeCpa(CpaProfile cpa) {
        int credits = 0;
        double totalWeighted = 0.0;

        for (GpaProfile g : cpa.getGpaProfiles()) {
            credits += g.getPassedCredits();
            totalWeighted += g.getTotalWeightedScore() != null ? g.getTotalWeightedScore() : 0.0;
        }

        cpa.setAccumulatedCredits(credits);
        cpa.setTotalAccumulatedScore(totalWeighted);

        if (credits == 0) {
            cpa.setNumberCpaScore(null);
            cpa.setLetterCpaScore(null);
        } else {
            double number = totalWeighted / credits;
            cpa.setNumberCpaScore(number);
            cpa.setLetterCpaScore(Grade.fromScore(number));
        }
    }

    private String randomLetterGrade() {
        String[] pool = { "A", "B+", "B", "C+", "C", "D+", "D", "F" };
        return pool[random.nextInt(pool.length)];
    }

    private String randomBetterOrSame(String base) {
        List<String> order = List.of("F", "D", "D+", "C", "C+", "B", "B+", "A");
        int i = order.indexOf(base);
        if (i < 0)
            return base;
        int up = Math.min(order.size() - 1, i + random.nextInt(3));
        return order.get(up);
    }
}
