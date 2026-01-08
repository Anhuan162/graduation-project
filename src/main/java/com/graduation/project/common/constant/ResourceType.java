package com.graduation.project.common.constant;

public enum ResourceType {
  PERMISSION("permissions/"),
  FACULTY("faculties/"),
  CLASSROOM("classrooms/"),
  USER("users/"),
  ROLE("roles/"),
  NOTIFICATION_EVENT("notifications/"),
  ANNOUNCEMENT("announcements/"),
  FILE("files/"),
  POST("posts/"),
  CATEGORY("categories/"),
  TOPIC("topics/"),
  COMMENT("comments/"),
  TOPIC_MEMBER("topic-members/"),
  REACTION("reactions/"),
  REPORT("reports/");

  private final String folderName;

  ResourceType(String folderName) {
    this.folderName = folderName;
  }

  public String getFolderName() {
    return folderName;
  }
}
