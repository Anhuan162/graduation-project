package com.graduation.project.auth.permission_handler;

import com.graduation.project.auth.security.EntityPermissionHandler;
import com.graduation.project.auth.security.UserPrincipal;
import com.graduation.project.common.entity.FileMetadata;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataPermissionHandler implements EntityPermissionHandler<FileMetadata> {
  @Override
  public boolean supports(Class<?> clazz) {
    return FileMetadata.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean hasPermission(
      Authentication authentication, FileMetadata target, String permission) {
    if (!(authentication.getPrincipal() instanceof UserPrincipal user)) return false;

    if (target instanceof FileMetadata fileMetadata) {
      String action = permission;

      return switch (action) {
        case "VIEW" -> true;
        case "UPDATE" ->
            user.hasAuthority("UPDATE_ALL_FILES")
                || (user.hasAuthority("UPDATE_ANY_FILES")
                    && fileMetadata.getUser().getId().equals(user.getId()));
        case "DELETE" ->
            user.hasAuthority("DELETE_ALL_FILES")
                || (user.hasAuthority("DELETE_ANY_FILES")
                    && fileMetadata.getUser().getId().equals(user.getId()));
        default -> false;
      };
    }
    return false;
  }
}
