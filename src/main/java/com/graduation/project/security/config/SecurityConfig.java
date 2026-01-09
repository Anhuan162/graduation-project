package com.graduation.project.security.config;

import com.graduation.project.auth.security.OAuth2AuthenticationSuccessHandler;
import com.graduation.project.common.config.CustomPermissionEvaluator;
import com.graduation.project.security.ultilities.JwtAuthenticationEntryPoint;
import com.graduation.project.security.ultilities.JwtToUserPrincipalConverter;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
  @Autowired private CustomJwtDecoder customJwtDecoder;
  private final CustomPermissionEvaluator customPermissionEvaluator;

  public SecurityConfig(
      OAuth2AuthenticationSuccessHandler successHandler,
      CustomPermissionEvaluator customPermissionEvaluator) {
    this.oauth2SuccessHandler = successHandler;
    this.customPermissionEvaluator = customPermissionEvaluator;
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
            auth ->
                auth.requestMatchers(
                        "/api/auth/**",
                        "/oauth2/**",
                        "/login/**",
                        "/api/users/**",
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
                    // Allow public access to documents (CommonDocumentController)
                    .requestMatchers(HttpMethod.GET, "/api/documents/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/subjects/**")
                    .permitAll()
                    // Forum Public Access
                    .requestMatchers(HttpMethod.GET, "/api/categories/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/topics/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/posts/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/comments/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reactions/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/announcements/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(oauth2SuccessHandler))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

    httpSecurity.oauth2ResourceServer(
        oauth2 ->
            oauth2
                .jwt(
                    jwtConfigurer ->
                        jwtConfigurer
                            .decoder(customJwtDecoder)
                            .jwtAuthenticationConverter(new JwtToUserPrincipalConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

    return httpSecurity.build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

    return jwtAuthenticationConverter;
  }
}
