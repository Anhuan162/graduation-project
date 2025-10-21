package com.graduation.project.auth.security;

import com.graduation.project.common.entity.Permission;
import com.graduation.project.common.entity.Role;
import com.graduation.project.common.entity.User;
import java.util.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {

  private UUID id;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(
      UUID id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
  }

  public static UserPrincipal create(User user) {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

    for (Role role : user.getRoles()) {
      grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));

      for (Permission permission : role.getPermissions()) {
        grantedAuthorities.add(new SimpleGrantedAuthority(permission.getName()));
      }
    }
    return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), grantedAuthorities);
  }

  public boolean hasRole(String roleName) {
    return authorities.stream().anyMatch(a -> a.getAuthority().contains(roleName));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
