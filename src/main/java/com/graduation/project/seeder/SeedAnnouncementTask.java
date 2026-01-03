package com.graduation.project.seeder;

import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.entity.AnnouncementTarget;
import com.graduation.project.announcement.entity.Classroom;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.announcement.repository.ClassroomRepository;
import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.common.entity.User;
import com.graduation.project.auth.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedAnnouncementTask {

    private final AnnouncementRepository announcementRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    private final Faker faker = new Faker();

    public void run(SeedProperties props) {
        seedClassrooms();
        seedAnnouncements();
    }

    private void seedClassrooms() {
        List<String> codes = List.of("K66-01", "K66-02", "D21CQCN01-B", "K67-02");

        for (String code : codes) {
            if (classroomRepository.existsByClassCode(code))
                continue;

            Classroom c = new Classroom();
            c.setClassCode(code);
            c.setClassName("Lớp " + code);
            c.setStartedYear(2023);
            c.setEndedYear(2027);

            classroomRepository.save(c);
        }

        log.info(" - Seeded classrooms");
    }

    private void seedAnnouncements() {
        log.info(" - Forcing Announcement Seeding...");

        User creator = userRepository.findAll(Sort.by(Sort.Direction.ASC, "registrationDate")).stream().findFirst()
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        if (creator == null) {
            throw new RuntimeException(
                    "Seeding Failed: No users found in database to assign as creator for announcements.");
        }

        // 1. Seed Global Announcements (Visible to everyone)
        for (int i = 0; i < 5; i++) {
            createAnnouncement(creator, null, "Thông báo Chung: ");
        }

        // 2. Seed Class Specific (Visible to D21CQCN01-B only)
        for (int i = 0; i < 5; i++) {
            createAnnouncement(creator, "D21CQCN01-B", "Thông báo lớp D21: ");
        }

        // 3. Seed Class Specific (Visible to K66-01 only)
        for (int i = 0; i < 5; i++) {
            createAnnouncement(creator, "K66-01", "Thông báo lớp K66: ");
        }

        log.info(" - Seeded announcements (Global, D21, K66)");
    }

    private void createAnnouncement(User creator, String classroomCode, String prefix) {
        Announcement a = Announcement.builder()
                .title(prefix + faker.job().title())
                .content(faker.lorem().paragraph(3))
                .createdBy(creator)
                .createdDate(LocalDate.now().minusDays(faker.number().numberBetween(0, 30)))
                .announcementStatus(true)
                .announcementType(AnnouncementType.GENERAL)
                .build();

        if (classroomCode != null) {
            AnnouncementTarget target = AnnouncementTarget
                    .builder()
                    .classroomCode(classroomCode)
                    .announcement(a)
                    .build();
            if (a.getTargets() == null) {
                a.setTargets(new java.util.ArrayList<>());
            }
            a.getTargets().add(target);
        }

        announcementRepository.save(a);
    }
}
