package com.graduation.project.seeder;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {

    private boolean enabled = false;
    private boolean continueOnFailure = true;

    @PositiveOrZero
    private int users = 20;

    @PositiveOrZero
    private int categories = 5;

    @PositiveOrZero
    private int topicsPerCategory = 3;

    @PositiveOrZero
    private int postsPerTopic = 5;

    @PositiveOrZero
    private int commentsPerPost = 2;
}
