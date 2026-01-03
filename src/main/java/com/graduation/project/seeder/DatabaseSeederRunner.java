package com.graduation.project.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeederRunner implements CommandLineRunner {

    private final SeedProperties props;

    private final SeedAuthTask seedAuthTask;
    private final SeedAnnouncementTask seedAnnouncementTask;
    private final SeedForumTask seedForumTask;

    private final SeedLibraryTask seedLibraryTask;
    private final SeedCpaTask seedCpaTask;

    private final Environment env;

    @Override
    public void run(String... args) {
        log.info("Active profiles: {}", java.util.Arrays.toString(env.getActiveProfiles()));
        log.info("ENV app.seed.enabled = {}", env.getProperty("app.seed.enabled"));
        log.info("PROPS enabled = {}", props.isEnabled());
        if (!props.isEnabled()) {
            log.info("Seed disabled (app.seed.enabled=false).");
            return;
        }

        log.info("Seeding database... props={}", props);

        safeRun("SeedAuthTask", () -> seedAuthTask.run(props));
        safeRun("SeedAnnouncementTask", () -> seedAnnouncementTask.run(props));

        safeRun("SeedLibraryTask", () -> seedLibraryTask.run(props));

        safeRun("SeedCpaTask", () -> seedCpaTask.run(props));

        safeRun("SeedForumTask", () -> seedForumTask.run(props));

        log.info("Database seeding completed.");
    }

    private void safeRun(String name, Runnable fn) {
        try {
            fn.run();
        } catch (Exception ex) {
            log.error("Seeding {} failed", name, ex);
            throw new RuntimeException("Seeding " + name + " failed", ex);
        }
    }
}
