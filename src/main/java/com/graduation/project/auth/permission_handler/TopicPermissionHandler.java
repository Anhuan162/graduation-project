package com.graduation.project.auth.permission_handler;

import com.graduation.project.auth.security.EntityPermissionHandler;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.Topic;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class TopicPermissionHandler implements EntityPermissionHandler<Topic> {

  @Override
  public boolean supports(Class<?> clazz) {
    return Topic.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Topic target, String permission) {
    if (!(authentication.getPrincipal() instanceof UserPrincipal user)) return false;

    if (target instanceof Topic post) {
      String action = permission;

      return switch (action) {
        case "VIEW" -> true;
        case "UPDATE" ->
            user.hasAuthority("UPDATE_ANY_TOPICS")
                || (user.hasAuthority("UPDATE_OWN_TOPIC")
                    && post.getCreatedBy().getId().equals(user.getId()));
        case "DELETE" ->
            user.hasAuthority("DELETE_ANY_TOPICS")
                || (user.hasAuthority("DELETE_OWN_TOPIC")
                    && post.getCreatedBy().getId().equals(user.getId()));
        default -> false;
      };
    }
    return false;
  }
}
