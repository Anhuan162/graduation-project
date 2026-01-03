package com.graduation.project.forum.constant.TopicPermission;

import java.util.HashSet;
import java.util.Set;

import com.graduation.project.forum.constant.TopicRole;

public class TopicPermissions {

  public static final Set<String> BASIC_RIGHTS = Set.of("CREATE_POST", "CREATE_COMMENT", "CREATE_REACTION",
      "CREATE_REPORT");

  public static final Set<String> PRIVATE_RIGHTS = Set.of(
      "DELETE_OWN_TOPIC",
      "DELETE_OWN_POST",
      "DELETE_OWN_COMMENT",
      "DELETE_OWN_REACTION",
      "UPDATE_OWN_TOPIC",
      "UPDATE_OWN_POST",
      "UPDATE_OWN_COMMENT",
      "UPDATE_OWN_REACTION");

  public static final Set<String> MODERATOR_RIGHTS = Set.of(
      "DELETE_ANY_POST",
      "DELETE_ANY_COMMENT",
      "APPROVE_MEMBER",
      "KICK_MEMBER",
      "VIEW_REPORT",
      "RESOLVE_REPORT");

  public static Set<String> getPermissions(TopicRole role) {
    Set<String> perms = new HashSet<>();

    if (role == null)
      return perms;

    perms.addAll(TopicPermissions.BASIC_RIGHTS);
    perms.addAll(TopicPermissions.PRIVATE_RIGHTS);

    if (role == TopicRole.MANAGER) {
      perms.addAll(TopicPermissions.MODERATOR_RIGHTS);
    }

    return perms;
  }
}
