package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.TrackViewResponse;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.PostViewLog;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.PostViewLogRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostViewService {

    // Throttle duration - a view is only counted if the previous view was more than
    // 10 minutes ago
    private static final Duration VIEW_THROTTLE = Duration.ofMinutes(10);

    // Repository dependencies for data access
    private final PostRepository postRepository;
    private final PostViewLogRepository postViewLogRepository;

    // Service dependencies for current user context and authorization
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    /**
     * Tracks a post view with throttling mechanism to prevent view count inflation.
     * Uses atomic upsert pattern with DB-level UNIQUE constraint to prevent race
     * conditions.
     *
     * View counting rules:
     * - First view by a user: Always counted (+1 to view count)
     * - Subsequent views:
     * - If last view was more than VIEW_THROTTLE (10 minutes) ago: Counted (+1)
     * - If last view was within VIEW_THROTTLE: Not counted (only updates
     * lastViewedAt)
     *
     * Thread-safe: Uses try-insert/catch-constraint-violation pattern to ensure
     * only one PostViewLog per (post_id, viewer_key) and view count only increments
     * on successful insert.
     *
     * Authorization: User must have permission to view the topic OR be the post
     * creator.
     *
     * @param postId The UUID of the post being viewed
     * @return TrackViewResponse containing post ID, latest view count, and whether
     *         this view was counted
     * @throws AppException with POST_NOT_FOUND if post doesn't exist
     * @throws AppException with UNAUTHORIZED if user lacks viewing permission
     */
    @Transactional
    public TrackViewResponse trackView(UUID postId) {
        // Fetch the post or throw exception if not found
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Get current authenticated user
        User user = currentUserService.getCurrentUserEntity();

        // Authorization check - user must be able to view the topic OR be the post
        // creator
        if (!authorizationService.canViewTopic(post.getTopic(), user)
                && !authorizationService.isPostCreator(post, user)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Create unique identifier for this viewer (currently using user ID for
        // logged-in users)
        String viewerKey = buildViewerKey(user);

        Instant now = Instant.now();

        boolean counted = false; // Flag to track if this view was counted

        try {
            // Try to create a new PostViewLog entry (first view scenario)
            PostViewLog logEntity = PostViewLog.builder()
                    .id(UUID.randomUUID())
                    .post(post)
                    .viewerKey(viewerKey)
                    .firstViewedAt(now)
                    .lastViewedAt(now)
                    .viewCount(1L)
                    .build();
            postViewLogRepository.save(logEntity);

            // Only increment view count if save succeeded
            postRepository.updateViewCount(postId, 1);
            counted = true;
        } catch (DataIntegrityViolationException e) {
            // SUBSEQUENT VIEW SCENARIO: Constraint violation means this viewer has seen
            // this post before
            // Load the existing PostViewLog and handle throttling
            PostViewLog v = postViewLogRepository.findByPost_IdAndViewerKey(postId, viewerKey)
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION,
                            "PostViewLog should exist after constraint violation"));

            Instant last = v.getLastViewedAt();
            // Check if enough time has passed since last view (throttle check)
            boolean allowCount = (last == null) || last.plus(VIEW_THROTTLE).isBefore(now);

            if (allowCount) {
                // Throttle period has passed - count this view
                postViewLogRepository.touchAndIncrease(v.getId(), now);
                postRepository.updateViewCount(postId, 1);
                counted = true;
            } else {
                // Within throttle period - update lastViewedAt but don't increment count
                // This maintains activity tracking without inflating view counts
                v.setLastViewedAt(now);
                postViewLogRepository.save(v);
                counted = false;
            }
        }

        // Fetch the latest view count to ensure data consistency
        Long latest = postRepository.getViewCount(postId);
        return TrackViewResponse.builder()
                .postId(postId)
                .viewCount(latest == null ? 0L : latest)
                .counted(counted)
                .build();
    }

    /**
     * Builds a unique identifier for the viewer.
     * Currently supports logged-in users only (using user ID).
     * Future enhancement: Support anonymous users with browser fingerprinting.
     *
     * @param user The current user entity
     * @return String representation of the viewer key
     */
    private String buildViewerKey(User user) {
        // Current implementation: Use user ID for authenticated users
        // Potential future implementation for anonymous users:
        // return "anon:" + browserFingerprint;
        return "user:" + user.getId();
    }
}
