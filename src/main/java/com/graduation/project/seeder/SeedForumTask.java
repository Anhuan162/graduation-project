package com.graduation.project.seeder;

import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.*;
import com.graduation.project.forum.entity.*;
import com.graduation.project.forum.repository.*;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedForumTask {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final TopicMemberRepository topicMemberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;

    private final Faker faker = new Faker();

    @Transactional
    public void run(SeedProperties props) {

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            log.warn(" [ForumSeed] Skip: no users");
            return;
        }

        long beforeCategories = categoryRepository.count();
        long beforeTopics = topicRepository.count();
        long beforePosts = postRepository.count();
        long beforeComments = commentRepository.count();
        long beforeReactions = reactionRepository.count();

        log.info(
                " [ForumSeed] Before: categories={}, topics={}, posts={}, comments={}, reactions={}",
                beforeCategories, beforeTopics, beforePosts, beforeComments, beforeReactions);

        seedCategoriesTopicsPosts(props, users);

        long afterCategories = categoryRepository.count();
        long afterTopics = topicRepository.count();
        long afterPosts = postRepository.count();
        long afterComments = commentRepository.count();
        long afterReactions = reactionRepository.count();

        log.info(
                " [ForumSeed] After: categories={}, topics={}, posts={}, comments={}, reactions={}",
                afterCategories, afterTopics, afterPosts, afterComments, afterReactions);
    }

    private void seedCategoriesTopicsPosts(SeedProperties props, List<User> users) {

        if (categoryRepository.count() >= props.getCategories()) {
            log.info(" [ForumSeed] Skip: Forum already seeded (categories exist)");
            return;
        }

        for (int i = 0; i < props.getCategories(); i++) {

            User creator = pick(users);

            Category cat = Category.builder()
                    .name("Chủ đề " + faker.educator().course())
                    .description(faker.lorem().sentence())
                    .categoryType(pickCategoryType())
                    .creator(creator)
                    .build();

            cat.getManagers().addAll(
                    pickMany(users, faker.number().numberBetween(1, Math.min(4, users.size()))));

            cat = categoryRepository.save(cat);

            for (int t = 0; t < props.getTopicsPerCategory(); t++) {

                User topicOwner = pick(users);

                Topic topic = Topic.builder()
                        .category(cat)
                        .title(faker.book().title())
                        .content(faker.lorem().paragraph(2))
                        .topicVisibility(TopicVisibility.PUBLIC)
                        .createdAt(Instant.now().minusSeconds(
                                faker.number().numberBetween(0, 60 * 60 * 24 * 30)))
                        .lastModifiedAt(Instant.now())
                        .createdBy(topicOwner)
                        .deleted(false)
                        .build();

                topic = topicRepository.save(topic);

                TopicMember ownerMember = TopicMember.builder()
                        .topic(topic)
                        .user(topicOwner)
                        .topicRole(TopicRole.MANAGER)
                        .approved(true)
                        .joinedAt(Instant.now())
                        .build();

                topicMemberRepository.save(ownerMember);

                for (int p = 0; p < props.getPostsPerTopic(); p++) {

                    User author = pick(users);
                    User approver = pick(users);

                    String postTitle = faker.lorem().sentence(6);

                    Instant createdInstant = Instant.now()
                            .minusSeconds(faker.number().numberBetween(0, 60 * 60 * 24 * 60));
                    long approvalOffset = faker.number().numberBetween(0,
                            Math.min(60 * 60 * 24 * 14, Duration.between(createdInstant, Instant.now()).getSeconds()));
                    Instant approvedAt = createdInstant.plusSeconds(approvalOffset);

                    Post post = Post.builder()
                            .topic(topic)
                            .author(author)
                            .title(postTitle)
                            .content(faker.lorem().paragraph(4))
                            .postStatus(PostStatus.APPROVED)
                            .approvedBy(approver)
                            .approvedAt(approvedAt)
                            .createdDateTime(createdInstant)
                            .lastModifiedDateTime(Instant.now())
                            .deleted(false)
                            .reactionCount(0L)
                            .commentCount(0L)
                            .viewCount(0L)
                            .slug(slugify(postTitle + "-" + faker.lorem().word() + "-" + UUID.randomUUID()))
                            .build();

                    post = postRepository.save(post);

                    long commentCount = 0;
                    long postReactionCount = 0;

                    for (int c = 0; c < props.getCommentsPerPost(); c++) {

                        User commenter = pick(users);

                        Comment comment = Comment.builder()
                                .post(post)
                                .author(commenter)
                                .content(faker.lorem().sentence(14))
                                .createdDateTime(Instant.now().minusSeconds(
                                        faker.number().numberBetween(0, 60 * 60 * 24 * 60)))
                                .deleted(false)
                                .reactionCount(0L)
                                .build();

                        comment = commentRepository.save(comment);
                        commentCount++;

                        if (faker.bool().bool()) {
                            Reaction r = Reaction.builder()
                                    .user(pick(users))
                                    .targetId(comment.getId())
                                    .targetType(TargetType.COMMENT)
                                    .type(ReactionType.LIKE)
                                    .build();

                            if (safeSaveReaction(r)) {
                                comment.setReactionCount(comment.getReactionCount() + 1);
                                commentRepository.save(comment);
                            }
                        }
                    }

                    if (faker.bool().bool()) {
                        Reaction r = Reaction.builder()
                                .user(pick(users))
                                .targetId(post.getId())
                                .targetType(TargetType.POST)
                                .type(ReactionType.LIKE)
                                .build();

                        if (safeSaveReaction(r)) {
                            postReactionCount++;
                        }
                    }

                    post.setCommentCount(commentCount);
                    post.setReactionCount(post.getReactionCount() + postReactionCount);
                    postRepository.save(post);
                }
            }
        }

        log.info(" [ForumSeed] Seeded forum successfully");
    }

    private boolean safeSaveReaction(Reaction r) {
        try {
            reactionRepository.save(r);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.trace(
                    " [ForumSeed] Duplicate reaction skipped: targetId={}, userId={}",
                    r.getTargetId(), r.getUser().getId());
            return false;
        }
    }

    private User pick(List<User> users) {
        return users.get(faker.number().numberBetween(0, users.size()));
    }

    private Set<User> pickMany(List<User> users, int n) {
        if (users.isEmpty() || n <= 0)
            return new HashSet<>();
        n = Math.min(n, users.size());
        List<User> copy = new ArrayList<>(users);
        Collections.shuffle(copy);
        return new HashSet<>(copy.subList(0, n));
    }

    private CategoryType pickCategoryType() {
        CategoryType[] values = CategoryType.values();
        return values[faker.number().numberBetween(0, values.length)];
    }

    private String slugify(String input) {
        if (input == null)
            return "";

        String normalized = Normalizer
                .normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        String slug = normalized
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");

        if (slug.isBlank()) {
            return "post-" + UUID.randomUUID();
        }
        return slug;
    }
}
