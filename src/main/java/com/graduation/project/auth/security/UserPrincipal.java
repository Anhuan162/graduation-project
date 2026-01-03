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

  private String fullName;
  private String avatar;

  private Collection<? extends GrantedAuthority> authorities;

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  public String getAvatarUrl() {
    return avatar;
  }

  public UserPrincipal(
      UUID id,
      String email,
      String password,
      String fullName,
      String avatar,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.fullName = fullName;
    this.avatar = avatar;
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

    return new UserPrincipal(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getFullName(),
        user.getAvatarUrl(),
        grantedAuthorities);
  }

  public boolean hasRole(String roleName) {
    return authorities.stream().anyMatch(a -> a.getAuthority().contains(roleName));
  }

  public boolean hasAuthority(String authority) {
    return authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
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
