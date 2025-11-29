package com.graduation.project.event.service;

// -------------------- src/main/java/com/graduation/project/security/JwtService.java --------------------

/**
 * Simple interface that you must implement to extract user id from JWT.
 * Implementation depends on your JWT library and token shape.
 */
public interface JwtService {
    /** Return user id string (UUID as string) from token, or throw exception if invalid */
    String getUserIdFromToken(String token);
}