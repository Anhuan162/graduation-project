package com.graduation.project.security.config;

import com.graduation.project.common.config.CustomPermissionEvaluator;
import com.graduation.project.security.ultilities.JwtAuthenticationEntryPoint;
import com.graduation.project.security.ultilities.JwtToUserPrincipalConverter;
import com.graduation.project.auth.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
  private final CustomJwtDecoder customJwtDecoder;
  private final CustomPermissionEvaluator customPermissionEvaluator;
  private final JwtToUserPrincipalConverter jwtToUserPrincipalConverter;

  public SecurityConfig(
      OAuth2AuthenticationSuccessHandler successHandler,
      CustomJwtDecoder customJwtDecoder,
      CustomPermissionEvaluator customPermissionEvaluator,
      JwtToUserPrincipalConverter jwtToUserPrincipalConverter) {
    this.oauth2SuccessHandler = successHandler;
    this.customJwtDecoder = customJwtDecoder;
    this.customPermissionEvaluator = customPermissionEvaluator;
    this.jwtToUserPrincipalConverter = jwtToUserPrincipalConverter;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(customPermissionEvaluator);
    return handler;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(HttpMethod.POST, "/api/users/register")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/verify")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/resend")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/password/reset")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/otp")
                .permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/users/password/reset-confirm")
                .permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/users/change-password")
                .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/{userId}")
                .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/public/{userId}")
                .permitAll()
                .requestMatchers(
                    "/api/auth/**",
                    "/oauth2/**",
                    "/login/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html")
                .permitAll()
                .requestMatchers("/ws/notification/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/public/**")
                .permitAll()
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")
                .anyRequest()
                .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(oauth2SuccessHandler))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

    httpSecurity.oauth2ResourceServer(
        oauth2 -> oauth2
            .jwt(
                jwtConfigurer -> jwtConfigurer
                    .decoder(customJwtDecoder)
                    .jwtAuthenticationConverter(jwtToUserPrincipalConverter))
            .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

    return httpSecurity.build();
  }

}
