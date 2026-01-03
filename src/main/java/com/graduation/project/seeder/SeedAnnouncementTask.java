package com.graduation.project.seeder;

import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.entity.Classroom;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.announcement.repository.ClassroomRepository;
import com.graduation.project.common.entity.User;
import com.graduation.project.auth.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
        List<String> codes = List.of("K66-01", "K66-02", "K67-01", "K67-02");

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
        if (announcementRepository.count() >= 15) {
            log.info(" - Announcements already seeded");
            return;
        }

        User creator = userRepository.findAll(Sort.by(Sort.Direction.ASC, "registrationDate")).stream().findFirst()
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        if (creator == null) {
            log.warn("No users found in database, skipping announcement seeding");
            return;
        }

        for (int i = 0; i < 15; i++) {
            Announcement a = Announcement.builder()
                    .id(UUID.randomUUID())
                    .title("Thông báo: " + faker.university().name())
                    .content(faker.lorem().paragraph(3))
                    .createdBy(creator)
                    .createdDate(LocalDate.now().minusDays(faker.number().numberBetween(0, 30)))
                    .build();

            announcementRepository.save(a);
        }

        log.info(" - Seeded announcements");
    }
}
