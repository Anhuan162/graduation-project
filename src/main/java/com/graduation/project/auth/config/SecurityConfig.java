package com.graduation.project.auth.config;

import com.graduation.project.auth.security.JwtAuthenticationFilter;
import com.graduation.project.auth.security.JwtUtils;
import com.graduation.project.auth.security.OAuth2AuthenticationSuccessHandler;
import com.graduation.project.auth.security.RestAuthenticationEntryPoint;
import com.graduation.project.auth.service.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;
  private final JwtUtils jwtUtils;
  private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

  public SecurityConfig(
      CustomUserDetailsService uds,
      JwtUtils jwtUtils,
      OAuth2AuthenticationSuccessHandler successHandler) {
    this.userDetailsService = uds;
    this.jwtUtils = jwtUtils;
    this.oauth2SuccessHandler = successHandler;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);

    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**", "/oauth2/**", "/login/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/public/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(oauth2SuccessHandler))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(new RestAuthenticationEntryPoint()));

    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
