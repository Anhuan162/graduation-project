package com.graduation.project.auth.service;

import com.graduation.project.auth.entity.UserRelation;
import com.graduation.project.auth.entity.UserRelation.UserRelationId;
import com.graduation.project.auth.repository.UserRelationRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.User;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserRelationRepository userRelationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(UUID followerId, UUID targetId) {
        if (followerId.equals(targetId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "You cannot follow yourself");
        }

        if (!userRepository.existsById(followerId) || !userRepository.existsById(targetId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        try {
            UserRelation relation = new UserRelation();
            relation.setId(new UserRelationId(followerId, targetId));

            // Use proxies to set FKs without triggering SELECTs
            relation.setFollower(userRepository.getReferenceById(followerId));
            relation.setFollowing(userRepository.getReferenceById(targetId));

            userRelationRepository.saveAndFlush(relation);

            userRepository.incrementFollowingCount(followerId);
            userRepository.incrementFollowerCount(targetId);
        } catch (DataIntegrityViolationException e) {
            // Idempotent: Already following
            return;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Follow error: " + e.getMessage());
        }
    }

    @Transactional
    public void unfollow(UUID followerId, UUID targetId) {
        UserRelationId relationId = new UserRelationId(followerId, targetId);
        // Use exists check or findById directly if needed, but for delete we need
        // entity or ID.
        // JPA deleteById is efficient.
        if (userRelationRepository.existsById(relationId)) {
            userRelationRepository.deleteById(relationId);
            userRelationRepository.flush(); // Ensure delete is flushed before counts update

            userRepository.decrementFollowingCount(followerId);
            userRepository.decrementFollowerCount(targetId);
        } else {
            throw new IllegalStateException("Not following this user");
        }
    }

    public boolean isFollowing(UUID followerId, UUID targetId) {
        return userRelationRepository.existsById(new UserRelationId(followerId, targetId));
    }
}
