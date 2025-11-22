package com.graduation.project.auth.permission_handler;

import com.graduation.project.auth.security.EntityPermissionHandler;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.Post;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PostPermissionHandler implements EntityPermissionHandler<Post> {

  @Override
  public boolean supports(Class<?> clazz) {
    return Post.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Post target, String permission) {
    if (!(authentication.getPrincipal() instanceof UserPrincipal user)) return false;

    if (target instanceof Post post) {
      String action = permission;

      return switch (action) {
        case "VIEW" -> true;
        case "UPDATE" ->
            user.hasAuthority("UPDATE_ANY_POSTS")
                || (user.hasAuthority("UPDATE_OWN_POST")
                    && post.getAuthor().getId().equals(user.getId()));
        case "DELETE" ->
            user.hasAuthority("DELETE_ANY_POSTS")
                || (user.hasAuthority("DELETE_OWN_POST")
                    && post.getAuthor().getId().equals(user.getId()))
                || post.getTopic().getCreatedBy().getId().equals(user.getId());
        default -> false;
      };
    }
    return false;
  }
}
