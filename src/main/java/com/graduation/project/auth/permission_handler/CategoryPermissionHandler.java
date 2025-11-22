package com.graduation.project.auth.permission_handler;

import com.graduation.project.auth.security.EntityPermissionHandler;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.Category;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CategoryPermissionHandler implements EntityPermissionHandler<Category> {

  @Override
  public boolean supports(Class<?> clazz) {
    return Category.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Category target, String permission) {
    if (!(authentication.getPrincipal() instanceof UserPrincipal user)) return false;

    if (target instanceof Category category) {
      String action = permission;

      return switch (action) {
        case "VIEW" -> true;
        case "UPDATE" ->
            user.hasAuthority("UPDATE_ANY_CATEGORIES")
                || (user.hasAuthority("UPDATE_OWN_CATEGORY")
                    && category.getCreator().getId().equals(user.getId()));
        case "DELETE" ->
            user.hasAuthority("DELETE_ANY_CATEGORIES")
                || (user.hasAuthority("DELETE_OWN_CATEGORY")
                    && category.getCreator().getId().equals(user.getId()));
        default -> false;
      };
    }
    return false;
  }
}
